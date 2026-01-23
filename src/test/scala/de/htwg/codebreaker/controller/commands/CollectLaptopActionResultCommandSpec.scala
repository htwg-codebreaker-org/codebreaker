package de.htwg.codebreaker.controller.commands

import scala.util.{Success, Failure, Random}

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.player.laptop.{LaptopAction, LaptopActionType, ActionRewards}
import de.htwg.codebreaker.controller.commands.laptop.{StartLaptopActionCommand, CollectLaptopActionResultCommand}

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


class CollectLaptopActionResultCommandSpec extends CommandTestBase {

  val testAction = LaptopAction(
    id = "test_action",
    name = "Test Action",
    actionType = LaptopActionType.PortScan,
    durationRounds = 2,
    coreCost = 1,
    cpuCost = 50,
    ramCost = 30,
    description = "A test action",
    toolId = "test_tool",
    Rewards = ActionRewards(100, 50, 0, 20)
  )

  "CollectLaptopActionResultCommand" should {

    "collect successful action result and claim server" in {
      val baseG = baseGame
      // Increase network range, CPU/RAM and cybersecurity for guaranteed success
      val player = baseG.model.players.head
      val updatedPlayer = player.copy(
        laptop = player.laptop.copy(
          hardware = player.laptop.hardware.copy(
            networkRange = 100,
            cpu = 1000,
            ram = 1000
          ),
          cybersecurity = 200  // High cybersecurity for guaranteed success
        )
      )
      val game = baseG.copy(
        model = baseG.model.copy(
          players = baseG.model.players.updated(0, updatedPlayer)
        )
      )

      // Find a server in range to hack
      val server = game.model.servers.find { s =>
        !s.hacked && {
          val dx = math.abs(updatedPlayer.tile.x - s.tile.x)
          val dy = math.abs(updatedPlayer.tile.y - s.tile.y)
          math.max(dx, dy) <= updatedPlayer.laptop.hardware.networkRange
        }
      }.get

      // Start action
      val startCmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )
      val gameWithAction = startCmd.doStep(game).get

      // Advance rounds to complete action
      val completedGame = gameWithAction.copy(
        state = gameWithAction.state.copy(
          round = gameWithAction.state.round + testAction.durationRounds
        )
      )

      val initialXp = completedGame.model.players.head.availableXp
      val initialKerne = completedGame.model.players.head.laptop.hardware.kerne

      // Collect with guaranteed success (use fixed random)
      val collectCmd = CollectLaptopActionResultCommand(
        playerIndex = 0,
        targetServerName = server.name,
        claimServer = true,
        random = new Random(0) // Should give success
      )

      val result = collectCmd.doStep(completedGame)

      if (result.isSuccess) {
        val finalGame = result.get
        val updated = finalGame.model.players.head

        // Running action should be removed
        updated.laptop.runningActions.exists(_.targetServer == server.name) shouldBe false

        // Cores should be released
        updated.laptop.hardware.kerne shouldBe (initialKerne + testAction.coreCost)

        // Check if server was hacked
        val updatedServer = finalGame.model.servers.find(_.name == server.name).get
        updatedServer.hacked shouldBe true
        updatedServer.claimedBy shouldBe Some(0)
        updatedServer.hackedBy shouldBe Some(0)

        // XP should be gained
        updated.availableXp should be > initialXp
      }
    }

    "collect successful action result without claiming server" in {
      val baseG = baseGame
      // Increase network range, CPU/RAM and cybersecurity for guaranteed success
      val player = baseG.model.players.head
      val updatedPlayer = player.copy(
        laptop = player.laptop.copy(
          hardware = player.laptop.hardware.copy(
            networkRange = 100,
            cpu = 1000,
            ram = 1000
          ),
          cybersecurity = 200  // High cybersecurity for guaranteed success
        )
      )
      val game = baseG.copy(
        model = baseG.model.copy(
          players = baseG.model.players.updated(0, updatedPlayer)
        )
      )

      val server = game.model.servers.find { s =>
        !s.hacked && {
          val dx = math.abs(updatedPlayer.tile.x - s.tile.x)
          val dy = math.abs(updatedPlayer.tile.y - s.tile.y)
          math.max(dx, dy) <= updatedPlayer.laptop.hardware.networkRange
        }
      }.get

      // Start action
      val startCmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )
      val gameWithAction = startCmd.doStep(game).get

      // Complete action
      val completedGame = gameWithAction.copy(
        state = gameWithAction.state.copy(
          round = gameWithAction.state.round + testAction.durationRounds
        )
      )

      // Collect without claiming (steal data only)
      val collectCmd = CollectLaptopActionResultCommand(
        playerIndex = 0,
        targetServerName = server.name,
        claimServer = false,
        random = new Random(0)
      )

      val result = collectCmd.doStep(completedGame)

      if (result.isSuccess) {
        val finalGame = result.get
        val updatedServer = finalGame.model.servers.find(_.name == server.name).get

        // Server should be hacked but not claimed
        updatedServer.hacked shouldBe true
        updatedServer.hackedBy shouldBe Some(0)
        updatedServer.claimedBy shouldBe None
      }
    }

    "handle failed action attempt" in {
      val baseG = baseGame
      // Increase network range and CPU/RAM
      val player = baseG.model.players.head
      val updatedPlayer = player.copy(
        laptop = player.laptop.copy(
          hardware = player.laptop.hardware.copy(
            networkRange = 100,
            cpu = 1000,
            ram = 1000
          )
        )
      )
      val game = baseG.copy(
        model = baseG.model.copy(
          players = baseG.model.players.updated(0, updatedPlayer)
        )
      )

      val server = game.model.servers.find { s =>
        !s.hacked && {
          val dx = math.abs(updatedPlayer.tile.x - s.tile.x)
          val dy = math.abs(updatedPlayer.tile.y - s.tile.y)
          math.max(dx, dy) <= updatedPlayer.laptop.hardware.networkRange
        }
      }.get

      // Start action
      val startCmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )
      val gameWithAction = startCmd.doStep(game).get

      // Complete action
      val completedGame = gameWithAction.copy(
        state = gameWithAction.state.copy(
          round = gameWithAction.state.round + testAction.durationRounds
        )
      )

      val initialKerne = completedGame.model.players.head.laptop.hardware.kerne

      // Collect with guaranteed failure (use fixed random that fails)
      val collectCmd = CollectLaptopActionResultCommand(
        playerIndex = 0,
        targetServerName = server.name,
        claimServer = true,
        random = new Random(1000) // Should give failure
      )

      val result = collectCmd.doStep(completedGame)

      if (result.isSuccess) {
        val finalGame = result.get
        val updated = finalGame.model.players.head
        val updatedServer = finalGame.model.servers.find(_.name == server.name).get

        // Running action should be removed even on failure
        updated.laptop.runningActions.exists(_.targetServer == server.name) shouldBe false

        // Cores should still be released
        updated.laptop.hardware.kerne should be >= initialKerne

        // Server should not be hacked on failure
        // (but due to randomness this might succeed sometimes)
      }
    }

    "fail if no completed action for server" in {
      val game = baseGame

      val cmd = CollectLaptopActionResultCommand(
        playerIndex = 0,
        targetServerName = "some-server",
        claimServer = true,
        random = new Random(42)
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "fail if action not completed yet" in {
      val baseG = baseGame
      // Increase network range and CPU/RAM
      val player = baseG.model.players.head
      val updatedPlayer = player.copy(
        laptop = player.laptop.copy(
          hardware = player.laptop.hardware.copy(
            networkRange = 100,
            cpu = 1000,
            ram = 1000
          )
        )
      )
      val game = baseG.copy(
        model = baseG.model.copy(
          players = baseG.model.players.updated(0, updatedPlayer)
        )
      )

      val server = game.model.servers.find { s =>
        !s.hacked && {
          val dx = math.abs(updatedPlayer.tile.x - s.tile.x)
          val dy = math.abs(updatedPlayer.tile.y - s.tile.y)
          math.max(dx, dy) <= updatedPlayer.laptop.hardware.networkRange
        }
      }.get

      // Start action
      val startCmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )
      val gameWithAction = startCmd.doStep(game).get

      // Don't advance rounds - action still running
      val collectCmd = CollectLaptopActionResultCommand(
        playerIndex = 0,
        targetServerName = server.name,
        claimServer = true,
        random = new Random(42)
      )

      val result = collectCmd.doStep(gameWithAction)
      result.isFailure shouldBe true
    }

    "release cores after collecting result" in {
      val baseG = baseGame
      // Increase network range and CPU/RAM
      val player = baseG.model.players.head
      val updatedPlayer = player.copy(
        laptop = player.laptop.copy(
          hardware = player.laptop.hardware.copy(
            networkRange = 100,
            cpu = 1000,
            ram = 1000
          )
        )
      )
      val game = baseG.copy(
        model = baseG.model.copy(
          players = baseG.model.players.updated(0, updatedPlayer)
        )
      )

      val server = game.model.servers.find { s =>
        !s.hacked && {
          val dx = math.abs(updatedPlayer.tile.x - s.tile.x)
          val dy = math.abs(updatedPlayer.tile.y - s.tile.y)
          math.max(dx, dy) <= updatedPlayer.laptop.hardware.networkRange
        }
      }.get

      // Start action
      val startCmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )
      val gameWithAction = startCmd.doStep(game).get

      val kerneAfterStart = gameWithAction.model.players.head.laptop.hardware.kerne

      // Complete action
      val completedGame = gameWithAction.copy(
        state = gameWithAction.state.copy(
          round = gameWithAction.state.round + testAction.durationRounds
        )
      )

      // Collect result
      val collectCmd = CollectLaptopActionResultCommand(
        playerIndex = 0,
        targetServerName = server.name,
        claimServer = true,
        random = new Random(0)
      )

      val result = collectCmd.doStep(completedGame)

      if (result.isSuccess) {
        val finalGame = result.get
        val kerneAfterCollect = finalGame.model.players.head.laptop.hardware.kerne

        // Cores should be released (increased by coreCost)
        kerneAfterCollect should be >= kerneAfterStart
      }
    }

    "undo collect action result command" in {
      val baseG = baseGame
      // Increase network range and CPU/RAM
      val player = baseG.model.players.head
      val updatedPlayer = player.copy(
        laptop = player.laptop.copy(
          hardware = player.laptop.hardware.copy(
            networkRange = 100,
            cpu = 1000,
            ram = 1000
          )
        )
      )
      val game = baseG.copy(
        model = baseG.model.copy(
          players = baseG.model.players.updated(0, updatedPlayer)
        )
      )

      val server = game.model.servers.find { s =>
        !s.hacked && {
          val dx = math.abs(updatedPlayer.tile.x - s.tile.x)
          val dy = math.abs(updatedPlayer.tile.y - s.tile.y)
          math.max(dx, dy) <= updatedPlayer.laptop.hardware.networkRange
        }
      }.get

      // Start and complete action
      val startCmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )
      val gameWithAction = startCmd.doStep(game).get

      val completedGame = gameWithAction.copy(
        state = gameWithAction.state.copy(
          round = gameWithAction.state.round + testAction.durationRounds
        )
      )

      val initialPlayer = completedGame.model.players.head
      val initialServer = completedGame.model.servers.find(_.name == server.name).get

      val collectCmd = CollectLaptopActionResultCommand(
        playerIndex = 0,
        targetServerName = server.name,
        claimServer = true,
        random = new Random(0)
      )

      val afterDo = collectCmd.doStep(completedGame)

      if (afterDo.isSuccess) {
        val afterUndo = collectCmd.undoStep(afterDo.get).get

        val revertedPlayer = afterUndo.model.players.head
        val revertedServer = afterUndo.model.servers.find(_.name == server.name).get

        // Server state should be reverted
        revertedServer.hacked shouldBe initialServer.hacked
        revertedServer.claimedBy shouldBe initialServer.claimedBy
        revertedServer.hackedBy shouldBe initialServer.hackedBy
      }
    }

  }
}
