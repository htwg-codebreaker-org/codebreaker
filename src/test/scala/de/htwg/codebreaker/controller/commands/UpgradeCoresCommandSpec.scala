package de.htwg.codebreaker.controller.commands

import scala.util.{Success, Failure}

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.controller.commands.laptop.UpgradeCoresCommand

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


class UpgradeCoresCommandSpec extends CommandTestBase {

  "UpgradeCoresCommand" should {

    "upgrade cores and reduce CPU" in {
      val game = baseGame
      val player = game.model.players.head
      val initialCores = player.laptop.hardware.kerne

      val expectedCost = UpgradeCoresCommand.calculateCost(initialCores)

      // Give player enough CPU for the upgrade
      val playerWithCpu = player.copy(
        laptop = player.laptop.copy(
          hardware = player.laptop.hardware.copy(cpu = expectedCost + 100)
        )
      )
      val gameWithCpu = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, playerWithCpu)
        )
      )
      val initialCpu = playerWithCpu.laptop.hardware.cpu

      val cmd = UpgradeCoresCommand(playerIndex = 0)
      val result = cmd.doStep(gameWithCpu).get

      val updated = result.model.players.head
      updated.laptop.hardware.kerne shouldBe (initialCores + 1)
      updated.laptop.hardware.cpu shouldBe (initialCpu - expectedCost)
    }

    "calculate cost correctly (quadratic)" in {
      UpgradeCoresCommand.calculateCost(1) shouldBe 100
      UpgradeCoresCommand.calculateCost(2) shouldBe 400
      UpgradeCoresCommand.calculateCost(3) shouldBe 900
      UpgradeCoresCommand.calculateCost(4) shouldBe 1600
    }

    "fail if not enough CPU" in {
      val game = baseGame
      val player = game.model.players.head

      // Set CPU to very low value
      val updatedHardware = player.laptop.hardware.copy(cpu = 10)
      val updatedLaptop = player.laptop.copy(hardware = updatedHardware)
      val updatedPlayer = player.copy(laptop = updatedLaptop)
      val gameWithLowCpu = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, updatedPlayer)
        )
      )

      val cmd = UpgradeCoresCommand(playerIndex = 0)
      val result = cmd.doStep(gameWithLowCpu)

      result.isFailure shouldBe true
    }

    "fail with invalid player index" in {
      val game = baseGame

      val cmd = UpgradeCoresCommand(playerIndex = 99)
      val result = cmd.doStep(game)

      result.isFailure shouldBe true
    }

    "undo upgrade command" in {
      val game = baseGame
      val player = game.model.players.head
      val initialCores = player.laptop.hardware.kerne

      // Give player enough CPU for the upgrade
      val upgradeCost = UpgradeCoresCommand.calculateCost(initialCores)
      val playerWithCpu = player.copy(
        laptop = player.laptop.copy(
          hardware = player.laptop.hardware.copy(cpu = upgradeCost + 100)
        )
      )
      val gameWithCpu = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, playerWithCpu)
        )
      )
      val initialCpu = playerWithCpu.laptop.hardware.cpu

      val cmd = UpgradeCoresCommand(playerIndex = 0)
      val afterDo = cmd.doStep(gameWithCpu).get
      val afterUndo = cmd.undoStep(afterDo).get

      val reverted = afterUndo.model.players.head
      reverted.laptop.hardware.kerne shouldBe initialCores
      reverted.laptop.hardware.cpu shouldBe initialCpu
    }

    "handle multiple upgrades with increasing cost" in {
      val game = baseGame
      val player = game.model.players.head

      // Give player lots of CPU
      val richPlayer = player.copy(
        laptop = player.laptop.copy(
          hardware = player.laptop.hardware.copy(cpu = 10000)
        )
      )
      val richGame = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, richPlayer)
        )
      )

      // First upgrade
      val cmd1 = UpgradeCoresCommand(0)
      val game1 = cmd1.doStep(richGame).get
      val cores1 = game1.model.players.head.laptop.hardware.kerne

      // Second upgrade should cost more
      val cmd2 = UpgradeCoresCommand(0)
      val game2 = cmd2.doStep(game1).get
      val cores2 = game2.model.players.head.laptop.hardware.kerne
      val cpu2 = game2.model.players.head.laptop.hardware.cpu

      cores2 shouldBe (cores1 + 1)

      // Verify cost increased (quadratic growth)
      val cost1 = UpgradeCoresCommand.calculateCost(richPlayer.laptop.hardware.kerne)
      val cost2 = UpgradeCoresCommand.calculateCost(cores1)
      cost2 should be > cost1
    }

  }
}
