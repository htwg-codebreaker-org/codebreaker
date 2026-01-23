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

    "reset movement points for next player" in {
      val game = baseGame
      val nextPlayerIndex = 1
      val nextPlayer = game.model.players(nextPlayerIndex)
      val maxMovement = nextPlayer.maxMovementPoints

      // Reduce movement points of the next player
      val reducedPlayer = nextPlayer.copy(movementPoints = 1)
      val gameWithReducedMovement = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(nextPlayerIndex, reducedPlayer)
        )
      )

      val cmd = NextPlayerCommand()
      val result = cmd.doStep(gameWithReducedMovement).get

      val refreshedPlayer = result.model.players(nextPlayerIndex)
      refreshedPlayer.movementPoints shouldBe maxMovement
    }

    "handle round increment correctly at wrap-around" in {
      val base = baseGame
      val lastPlayerIndex = base.model.players.length - 1
      val currentRound = base.state.round

      val game = base.copy(
        state = base.state.copy(currentPlayerIndex = Some(lastPlayerIndex))
      )

      val cmd = NextPlayerCommand()
      val result = cmd.doStep(game).get

      result.state.currentPlayerIndex shouldBe Some(0)
      result.state.round shouldBe (currentRound + 1)
    }

    "not increase round for mid-turn transitions" in {
      val base = baseGame
      val currentRound = base.state.round

      // Ensure we're not at the last player
      val game = base.copy(
        state = base.state.copy(currentPlayerIndex = Some(0))
      )

      val cmd = NextPlayerCommand()
      val result = cmd.doStep(game).get

      result.state.round shouldBe currentRound
    }

    "handle default player index (None -> 0)" in {
      val base = baseGame
      val game = base.copy(
        state = base.state.copy(currentPlayerIndex = None)
      )

      val cmd = NextPlayerCommand()
      val result = cmd.doStep(game).get

      // When currentPlayerIndex is None, it defaults to 0, so next should be 1
      result.state.currentPlayerIndex shouldBe Some(1)
    }

    "undo next player step" in {
      val game = baseGame
      val cmd = NextPlayerCommand()

      val afterDo = cmd.doStep(game).get
      val afterUndo = cmd.undoStep(afterDo).get

      afterUndo.state.currentPlayerIndex shouldBe Some(0)
      afterUndo.state.round shouldBe 1
    }

    "restore movement points on undo" in {
      val game = baseGame
      val nextPlayerIndex = 1
      val nextPlayer = game.model.players(nextPlayerIndex)

      // Reduce movement points before executing command
      val reducedPlayer = nextPlayer.copy(movementPoints = 3)
      val gameWithReducedMovement = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(nextPlayerIndex, reducedPlayer)
        )
      )

      val cmd = NextPlayerCommand()
      val afterDo = cmd.doStep(gameWithReducedMovement).get
      val afterUndo = cmd.undoStep(afterDo).get

      val restoredPlayer = afterUndo.model.players(nextPlayerIndex)
      restoredPlayer.movementPoints shouldBe 3
    }

    "throw exception when undoing without previous state" in {
      val game = baseGame
      val cmd = NextPlayerCommand()

      // Try to undo without calling doStep first
      val result = cmd.undoStep(game)
      result.isFailure shouldBe true
    }
  }
}
