package de.htwg.codebreaker.controller.commands

import scala.util.{Success, Failure, Random}

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.player.laptop.LaptopTool
import de.htwg.codebreaker.controller.commands.laptop.SearchInternetCommand

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


class SearchInternetCommandSpec extends CommandTestBase {

  "SearchInternetCommand" should {

    "start an internet search and reduce code" in {
      val game = baseGame
      val player = game.model.players.head
      val initialCode = player.laptop.hardware.code

      val cmd = SearchInternetCommand(
        playerIndex = 0,
        codeCost = 20,
        durationRounds = 2,
        random = new Random(42)
      )

      val result = cmd.doStep(game).get

      val updated = result.model.players.head
      updated.laptop.hardware.code shouldBe (initialCode - 20)
      updated.laptop.runningInternetSearch should not be None

      val search = updated.laptop.runningInternetSearch.get
      search.startRound shouldBe game.state.round
      search.completionRound shouldBe (game.state.round + 2)
      search.foundTools should not be empty
    }

    "fail if not enough code" in {
      val game = baseGame
      val player = game.model.players.head

      // Set code to 0
      val updatedHardware = player.laptop.hardware.copy(code = 0)
      val updatedLaptop = player.laptop.copy(hardware = updatedHardware)
      val updatedPlayer = player.copy(laptop = updatedLaptop)
      val gameWithNoCode = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, updatedPlayer)
        )
      )

      val cmd = SearchInternetCommand(
        playerIndex = 0,
        codeCost = 20,
        random = new Random(42)
      )

      val result = cmd.doStep(gameWithNoCode)
      result.isFailure shouldBe true
    }

    "fail if search already running" in {
      val game = baseGame

      // Start first search
      val cmd1 = SearchInternetCommand(
        playerIndex = 0,
        codeCost = 20,
        random = new Random(42)
      )
      val gameWithSearch = cmd1.doStep(game).get

      // Try to start second search
      val cmd2 = SearchInternetCommand(
        playerIndex = 0,
        codeCost = 20,
        random = new Random(42)
      )

      val result = cmd2.doStep(gameWithSearch)
      result.isFailure shouldBe true
    }

    "fail with invalid player index" in {
      val game = baseGame
      val cmd = SearchInternetCommand(
        playerIndex = 99,
        codeCost = 20,
        random = new Random(42)
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "undo search command" in {
      val game = baseGame
      val player = game.model.players.head
      val initialCode = player.laptop.hardware.code

      val cmd = SearchInternetCommand(
        playerIndex = 0,
        codeCost = 20,
        random = new Random(42)
      )

      val afterDo = cmd.doStep(game).get
      val afterUndo = cmd.undoStep(afterDo).get

      val reverted = afterUndo.model.players.head
      reverted.laptop.hardware.code shouldBe initialCode
      reverted.laptop.runningInternetSearch shouldBe None
    }

  }
}
