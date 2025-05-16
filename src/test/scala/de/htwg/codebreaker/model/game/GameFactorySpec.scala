package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game.strategy._

class GameFactorySpec extends AnyWordSpec with Matchers {

  object DummyPlayerStrategy extends PlayerGenerationStrategy {
    override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] = {
      (0 until numPlayers).toList.map { i =>
        Player(i, s"Player$i", Tile(i, i, Continent.Europe), 10, 10, 10, 1, 0, 5)
      }
    }
  }

  object DummyServerStrategy extends ServerGenerationStrategy {
    override def generateServers(map: WorldMap): List[Server] = {
      List(
        Server("S1", Tile(0, 0, Continent.Europe), 10, 10, 10, false, ServerType.Cloud)
      )
    }
  }

  "GameFactory" should {
    "create a game using custom strategies" in {
      val (model, state) = GameFactory.createGameWithStrategies(DummyPlayerStrategy, DummyServerStrategy)

      model.players should have size 2
      model.servers should have size 1
      model.worldMap should not be null
      state shouldBe a [GameState]
    }

    "create a default game with predefined strategies" in {
      val (model, state) = GameFactory.createDefaultGame()

      model.players should have size 2
      model.servers should not be empty
      model.worldMap should not be null
      state shouldBe a [GameState]
    }
  }
}
