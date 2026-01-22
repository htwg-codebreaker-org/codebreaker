package de.htwg.codebreaker.controller.commands

import scala.util.Success

import de.htwg.codebreaker.model.game.{Game, GameState, GameStatus, Phase}
import de.htwg.codebreaker.controller.commands.player.NextPlayerCommand

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


class NextPlayerCommandSpec extends CommandTestBase {

  "NextPlayerCommand" should {

    "advance to the next player" in {
      val game = baseGame
      val cmd = NextPlayerCommand()

      val result = cmd.doStep(game).get

      result.state.currentPlayerIndex shouldBe Some(1)
      result.state.round shouldBe 1
    }

    "should increase round after last player" in {
      val base = baseGame
      val game = base.copy(
        state = base.state.copy(currentPlayerIndex = Some(1))
      )

      val cmd = NextPlayerCommand()
      val result = cmd.doStep(game).get

      result.state.currentPlayerIndex shouldBe Some(0)
      result.state.round shouldBe 2
    }


    "undo next player step" in {
      val game = baseGame
      val cmd = NextPlayerCommand()

      val afterDo = cmd.doStep(game).get
      val afterUndo = cmd.undoStep(afterDo).get

      afterUndo.state.currentPlayerIndex shouldBe Some(0)
      afterUndo.state.round shouldBe 1
    }
  }
}
