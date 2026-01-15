package de.htwg.codebreaker.model.game

import de.htwg.codebreaker.model._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameSpec extends AnyWordSpec with Matchers {

  val tile     = Tile(0, 0, Continent.Europe)
  val player   = Player(0, "Test", tile, 1, 1, 1, 1, 0, 0)
  val server   = Server("S1", tile, 10, 2, 3, false, ServerType.Bank)
  val worldMap = WorldMap(1, 1, Vector(tile))
  val model    = GameModel(List(player), List(server), worldMap)
  val state    = GameState()

  "A Game" should {
    "be created with model and state" in {
      val game = Game(model, state)
      game.model shouldBe model
      game.state shouldBe state
    }

    "support copy" in {
      val game     = Game(model, state)
      val newState = state.copy(round = 5)
      val newGame  = game.copy(state = newState)

      newGame.model shouldBe model
      newGame.state.round shouldBe 5
    }

    "support equality" in {
      val game1 = Game(model, state)
      val game2 = Game(model, state)
      game1 shouldBe game2
    }
  }
}
