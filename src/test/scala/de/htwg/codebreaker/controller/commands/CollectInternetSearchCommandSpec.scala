package de.htwg.codebreaker.controller.commands

import scala.util.{Success, Failure, Random}

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.player.laptop.{LaptopTool, RunningInternetSearch, LaptopAction}
import de.htwg.codebreaker.controller.commands.laptop.{SearchInternetCommand, CollectInternetSearchCommand}

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


class CollectInternetSearchCommandSpec extends CommandTestBase {

  "CollectInternetSearchCommand" should {

    "collect selected tools and grant XP" in {
      val game = baseGame

      // Start a search first
      val searchCmd = SearchInternetCommand(
        playerIndex = 0,
        codeCost = 20,
        durationRounds = 2,
        random = new Random(42)
      )
      val gameWithSearch = searchCmd.doStep(game).get

      // Advance rounds to complete the search
      val completedGame = gameWithSearch.copy(
        state = gameWithSearch.state.copy(
          round = gameWithSearch.state.round + 2
        )
      )

      val player = completedGame.model.players.head
      val search = player.laptop.runningInternetSearch.get
      val toolIds = search.foundTools.map(_.id)

      val initialXp = player.availableXp
      val initialTotalXp = player.totalXpEarned

      // Collect all found tools
      val collectCmd = CollectInternetSearchCommand(
        playerIndex = 0,
        selectedToolIds = toolIds
      )

      val result = collectCmd.doStep(completedGame).get

      val updated = result.model.players.head
      updated.laptop.runningInternetSearch shouldBe None
      updated.laptop.tools.installedTools.size should be > player.laptop.tools.installedTools.size

      // Check XP gained (10 XP per tool)
      val expectedXp = toolIds.length * 10
      updated.availableXp shouldBe (initialXp + expectedXp)
      updated.totalXpEarned shouldBe (initialTotalXp + expectedXp)
    }

    "allow discarding all tools" in {
      val game = baseGame

      // Start a search
      val searchCmd = SearchInternetCommand(
        playerIndex = 0,
        codeCost = 20,
        durationRounds = 2,
        random = new Random(42)
      )
      val gameWithSearch = searchCmd.doStep(game).get

      // Advance rounds
      val completedGame = gameWithSearch.copy(
        state = gameWithSearch.state.copy(
          round = gameWithSearch.state.round + 2
        )
      )

      val player = completedGame.model.players.head
      val initialToolCount = player.laptop.tools.installedTools.size
      val initialXp = player.availableXp

      // Collect with empty list (discard all)
      val collectCmd = CollectInternetSearchCommand(
        playerIndex = 0,
        selectedToolIds = List.empty
      )

      val result = collectCmd.doStep(completedGame).get

      val updated = result.model.players.head
      updated.laptop.runningInternetSearch shouldBe None
      updated.laptop.tools.installedTools.size shouldBe initialToolCount
      updated.availableXp shouldBe initialXp  // No XP if discarded
    }

    "fail if no running search" in {
      val game = baseGame

      val cmd = CollectInternetSearchCommand(
        playerIndex = 0,
        selectedToolIds = List("some-tool")
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "fail if search not completed yet" in {
      val game = baseGame

      // Start search
      val searchCmd = SearchInternetCommand(
        playerIndex = 0,
        codeCost = 20,
        durationRounds = 2,
        random = new Random(42)
      )
      val gameWithSearch = searchCmd.doStep(game).get

      // Don't advance rounds - search still running
      val collectCmd = CollectInternetSearchCommand(
        playerIndex = 0,
        selectedToolIds = List("some-tool")
      )

      val result = collectCmd.doStep(gameWithSearch)
      result.isFailure shouldBe true
    }

    "undo collect command" in {
      val game = baseGame

      // Start and complete a search
      val searchCmd = SearchInternetCommand(
        playerIndex = 0,
        codeCost = 20,
        durationRounds = 2,
        random = new Random(42)
      )
      val gameWithSearch = searchCmd.doStep(game).get

      val completedGame = gameWithSearch.copy(
        state = gameWithSearch.state.copy(
          round = gameWithSearch.state.round + 2
        )
      )

      val player = completedGame.model.players.head
      val toolIds = player.laptop.runningInternetSearch.get.foundTools.map(_.id)
      val initialXp = player.availableXp
      val initialToolCount = player.laptop.tools.installedTools.size

      val collectCmd = CollectInternetSearchCommand(
        playerIndex = 0,
        selectedToolIds = toolIds
      )

      val afterDo = collectCmd.doStep(completedGame).get
      val afterUndo = collectCmd.undoStep(afterDo).get

      val reverted = afterUndo.model.players.head
      reverted.laptop.runningInternetSearch should not be None
      reverted.laptop.tools.installedTools.size shouldBe initialToolCount
      reverted.availableXp shouldBe initialXp
    }

  }
}
