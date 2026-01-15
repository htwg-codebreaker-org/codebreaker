package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class NextPlayerCommandSpec extends AnyWordSpec with Matchers {

  val tile    = Tile(0, 0, Continent.Europe)
  val players = List(
    Player(0, "Alice", tile, 1, 1, 1, 1, 0, 0),
    Player(1, "Bob", tile, 1, 1, 1, 1, 0, 0)
  )
  val model   = GameModel(players, Nil, WorldMap(1, 1, Vector(tile)))

  "A NextPlayerCommand" should {

    "advance to next player and keep round" in {
      val game = Game(model, GameState(currentPlayerIndex = Some(0), round = 1))
      val cmd  = NextPlayerCommand()

      val result = cmd.doStep(game)
      result.isSuccess shouldBe true
      result.get.state.currentPlayerIndex shouldBe Some(1)
      result.get.state.round shouldBe 1
    }

    "wrap to first player and increment round" in {
      val game = Game(model, GameState(currentPlayerIndex = Some(1), round = 3))
      val cmd  = NextPlayerCommand()

      val result = cmd.doStep(game)
      result.isSuccess shouldBe true
      result.get.state.currentPlayerIndex shouldBe Some(0)
      result.get.state.round shouldBe 4
    }

    "undo from player 0 to player 1 and decrement round" in {
      val game = Game(model, GameState(currentPlayerIndex = Some(0), round = 5))
      val cmd  = NextPlayerCommand()

      val result = cmd.undoStep(game)
      result.isSuccess shouldBe true
      result.get.state.currentPlayerIndex shouldBe Some(1)
      result.get.state.round shouldBe 4
    }

    "undo from player 1 to player 0 and keep round" in {
      val game = Game(model, GameState(currentPlayerIndex = Some(1), round = 5))
      val cmd  = NextPlayerCommand()

      val result = cmd.undoStep(game)
      result.isSuccess shouldBe true
      result.get.state.currentPlayerIndex shouldBe Some(0)
      result.get.state.round shouldBe 5
    }
  }
}
