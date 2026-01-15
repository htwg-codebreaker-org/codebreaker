package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model.game._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Success

class DummyCommand extends Command {
  override def doStep(game: Game)   = Success(
    game.copy(state = game.state.copy(round = game.state.round + 1))
  )
  override def undoStep(game: Game) = Success(
    game.copy(state = game.state.copy(round = game.state.round - 1))
  )
}

class CommandSpec extends AnyWordSpec with Matchers {
  "A Dummy Command" should {
    val game = GameFactory("default")
    val cmd  = new DummyCommand()

    "do a step" in {
      val result = cmd.doStep(game)
      result shouldBe a[Success[?]]
      result.get.state.round shouldBe (game.state.round + 1)
    }

    "undo a step" in {
      val result = cmd.undoStep(game)
      result shouldBe a[Success[?]]
      result.get.state.round shouldBe (game.state.round - 1)
    }
  }
}
