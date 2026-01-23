package de.htwg.codebreaker.controller.commands

import scala.util.{Success, Failure}

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.player.laptop.{LaptopAction, LaptopActionType, ActionRewards}
import de.htwg.codebreaker.controller.commands.laptop.StartLaptopActionCommand

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


class StartLaptopActionCommandSpec extends CommandTestBase {

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

  "StartLaptopActionCommand" should {

    "start a laptop action and reduce resources" in {
      val baseG = baseGame
      // Increase network range and CPU to ensure we can find and hack a server
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

      // Find a server in range (not necessarily same tile)
      val server = game.model.servers.find { s =>
        !s.hacked && {
          val dx = math.abs(updatedPlayer.tile.x - s.tile.x)
          val dy = math.abs(updatedPlayer.tile.y - s.tile.y)
          math.max(dx, dy) <= updatedPlayer.laptop.hardware.networkRange
        }
      }.get

      val initialCpu = updatedPlayer.laptop.hardware.cpu
      val initialRam = updatedPlayer.laptop.hardware.ram
      val initialKerne = updatedPlayer.laptop.hardware.kerne

      val cmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )

      val result = cmd.doStep(game).get

      val updated = result.model.players.head
      updated.laptop.hardware.cpu shouldBe (initialCpu - testAction.cpuCost)
      updated.laptop.hardware.ram shouldBe (initialRam - testAction.ramCost)
      updated.laptop.hardware.kerne shouldBe (initialKerne - testAction.coreCost)

      updated.laptop.runningActions should have size 1
      val runningAction = updated.laptop.runningActions.head
      runningAction.action shouldBe testAction
      runningAction.targetServer shouldBe server.name
      runningAction.startRound shouldBe game.state.round
      runningAction.completionRound shouldBe (game.state.round + testAction.durationRounds)
    }

    "fail if not enough CPU" in {
      val baseG = baseGame
      val player = baseG.model.players.head

      // Increase network range and set CPU to low value
      val updatedHardware = player.laptop.hardware.copy(cpu = 10, networkRange = 100)
      val updatedLaptop = player.laptop.copy(hardware = updatedHardware)
      val updatedPlayer = player.copy(laptop = updatedLaptop)
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

      val gameWithLowCpu = game

      val cmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )

      val result = cmd.doStep(gameWithLowCpu)
      result.isFailure shouldBe true
    }

    "fail if not enough RAM" in {
      val baseG = baseGame
      val player = baseG.model.players.head

      // Increase network range and set RAM to low value
      val updatedHardware = player.laptop.hardware.copy(ram = 10, networkRange = 100)
      val updatedLaptop = player.laptop.copy(hardware = updatedHardware)
      val updatedPlayer = player.copy(laptop = updatedLaptop)
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

      val gameWithLowRam = game

      val cmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )

      val result = cmd.doStep(gameWithLowRam)
      result.isFailure shouldBe true
    }

    "fail if not enough cores" in {
      val baseG = baseGame
      val player = baseG.model.players.head

      // Increase network range and set cores to 0
      val updatedHardware = player.laptop.hardware.copy(kerne = 0, networkRange = 100)
      val updatedLaptop = player.laptop.copy(hardware = updatedHardware)
      val updatedPlayer = player.copy(laptop = updatedLaptop)
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

      val gameWithNoCores = game

      val cmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )

      val result = cmd.doStep(gameWithNoCores)
      result.isFailure shouldBe true
    }

    "fail if server not found" in {
      val game = baseGame

      val cmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = "nonexistent-server"
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "fail if server already hacked" in {
      val baseG = baseGame
      // Increase network range and CPU
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

      // Find and hack a server
      val server = game.model.servers.find { s =>
        !s.hacked && {
          val dx = math.abs(updatedPlayer.tile.x - s.tile.x)
          val dy = math.abs(updatedPlayer.tile.y - s.tile.y)
          math.max(dx, dy) <= updatedPlayer.laptop.hardware.networkRange
        }
      }.get

      val hackedServer = server.copy(hacked = true)
      val gameWithHackedServer = game.copy(
        model = game.model.copy(
          servers = game.model.servers.map {
            case s if s.name == server.name => hackedServer
            case s => s
          }
        )
      )

      val cmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )

      val result = cmd.doStep(gameWithHackedServer)
      result.isFailure shouldBe true
    }

    "fail if server not in range" in {
      val game = baseGame
      val player = game.model.players.head

      // Find a server far away from player
      val farServer = game.model.servers.find { s =>
        val dx = math.abs(player.tile.x - s.tile.x)
        val dy = math.abs(player.tile.y - s.tile.y)
        math.max(dx, dy) > player.laptop.hardware.networkRange && !s.hacked
      }

      farServer match {
        case Some(server) =>
          val cmd = StartLaptopActionCommand(
            playerIndex = 0,
            action = testAction,
            targetServerName = server.name
          )

          val result = cmd.doStep(game)
          result.isFailure shouldBe true

        case None =>
          // If no far server found, test passes (all servers in range)
          succeed
      }
    }

    "fail with invalid player index" in {
      val game = baseGame

      val cmd = StartLaptopActionCommand(
        playerIndex = 99,
        action = testAction,
        targetServerName = "any-server"
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "undo start action command" in {
      val baseG = baseGame
      // Increase network range and CPU
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

      val initialCpu = updatedPlayer.laptop.hardware.cpu
      val initialRam = updatedPlayer.laptop.hardware.ram
      val initialKerne = updatedPlayer.laptop.hardware.kerne
      val initialActions = updatedPlayer.laptop.runningActions.size

      val cmd = StartLaptopActionCommand(
        playerIndex = 0,
        action = testAction,
        targetServerName = server.name
      )

      val afterDo = cmd.doStep(game).get
      val afterUndo = cmd.undoStep(afterDo).get

      val reverted = afterUndo.model.players.head
      reverted.laptop.hardware.cpu shouldBe initialCpu
      reverted.laptop.hardware.ram shouldBe initialRam
      reverted.laptop.hardware.kerne shouldBe initialKerne
      reverted.laptop.runningActions should have size initialActions
    }

  }
}
