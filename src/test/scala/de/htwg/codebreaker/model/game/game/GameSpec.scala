package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class GameSpec extends AnyWordSpec with Matchers:

  val testTile = Tile(10, 10, Continent.Europe)
  val testPlayer = Player(0, "Test", testTile, 100, 100, 0, 0, 0, PlayerSkillTree(Set.empty), 30)
  val testServer = Server("TestServer", testTile, 50, 100, 200, false, ServerType.Private)
  val testMap = WorldMap.defaultMap
  
  val testModel = GameModel(
    players = List(testPlayer),
    servers = List(testServer),
    worldMap = testMap,
    skills = Nil
  )
  
  val testState = GameState()

  "Game" should {

    "be created with model and state" in {
      val game = Game(testModel, testState)
      
      game.model shouldBe testModel
      game.state shouldBe testState
    }

    "provide access to model properties" in {
      val game = Game(testModel, testState)
      
      game.model.players should have size 1
      game.model.servers should have size 1
      game.model.worldMap shouldBe testMap
    }

    "provide access to state properties" in {
      val game = Game(testModel, testState)
      
      game.state.round shouldBe 0
      game.state.status shouldBe GameStatus.Running
      game.state.phase shouldBe Phase.AwaitingInput
    }

    "support updating state functionally" in {
      val original = Game(testModel, testState)
      val newState = testState.advanceRound()
      val updated = original.copy(state = newState)
      
      original.state.round shouldBe 0
      updated.state.round shouldBe 1
    }

    "support updating model functionally" in {
      val original = Game(testModel, testState)
      val updatedPlayer = testPlayer.copy(cpu = 200)
      val newModel = testModel.copy(players = List(updatedPlayer))
      val updated = original.copy(model = newModel)
      
      original.model.players.head.cpu shouldBe 100
      updated.model.players.head.cpu shouldBe 200
    }

    "support combining state and model updates" in {
      val original = Game(testModel, testState)
      
      val updatedPlayer = testPlayer.copy(cpu = 200)
      val newModel = testModel.copy(players = List(updatedPlayer))
      val newState = testState.advanceRound().setPhase(Phase.ExecutingTurn)
      val updated = original.copy(model = newModel, state = newState)
      
      updated.model.players.head.cpu shouldBe 200
      updated.state.round shouldBe 1
      updated.state.phase shouldBe Phase.ExecutingTurn
    }

    "support equality comparison" in {
      val game1 = Game(testModel, testState)
      val game2 = Game(testModel, testState)
      val game3 = Game(testModel, testState.advanceRound())
      
      game1 shouldBe game2
      game1 should not be game3
    }

    "maintain immutability" in {
      val original = Game(testModel, testState)
      val modified = original.copy(state = testState.advanceRound())
      
      original should not be modified
      original.state.round shouldBe 0
      modified.state.round shouldBe 1
    }
  }
