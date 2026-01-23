package de.htwg.codebreaker.controller.commands

import scala.util.{Success, Failure}

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.server.{ServerRoleType, ServerRoleBlueprint}
import de.htwg.codebreaker.controller.commands.server.InstallServerRoleCommand

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


class InstallServerRoleCommandSpec extends CommandTestBase {

  "InstallServerRoleCommand" should {

    "install a role on a claimed server" in {
      val game = baseGame

      // Find a server that is claimed by player 0
      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          // Ensure we have a role blueprint
          val roleBlueprint = game.model.roleBlueprints.headOption

          roleBlueprint match {
            case Some(blueprint) =>
              val cmd = InstallServerRoleCommand(
                playerIndex = 0,
                targetServerName = server.name,
                roleType = blueprint.roleType
              )

              val result = cmd.doStep(game).get

              val updatedServer = result.model.servers.find(_.name == server.name).get
              updatedServer.installedRole should not be None

              val role = updatedServer.installedRole.get
              role.roleType shouldBe blueprint.roleType
              role.isActive shouldBe false
              role.detectionRisk shouldBe blueprint.baseDetectionRisk
              role.runningActions shouldBe Nil
              role.networkRange shouldBe blueprint.networkRange

              // Server should be blocked during setup
              updatedServer.blockedUntilRound should not be None

            case None =>
              // No blueprints available, skip test
              succeed
          }

        case None =>
          // No claimed server found, skip test
          succeed
      }
    }

    "fail if server not found" in {
      val game = baseGame

      val cmd = InstallServerRoleCommand(
        playerIndex = 0,
        targetServerName = "nonexistent-server",
        roleType = ServerRoleType.BitcoinMiner
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "fail if server not claimed by player" in {
      val game = baseGame

      // Find a server not claimed by player 0
      val unclaimedServerOpt = game.model.servers.find(s =>
        !s.claimedBy.contains(0)
      )

      unclaimedServerOpt match {
        case Some(server) =>
          val cmd = InstallServerRoleCommand(
            playerIndex = 0,
            targetServerName = server.name,
            roleType = ServerRoleType.BitcoinMiner
          )

          val result = cmd.doStep(game)
          result.isFailure shouldBe true

        case None =>
          // All servers claimed by player 0, skip test
          succeed
      }
    }

    "fail if server already has a role" in {
      val game = baseGame

      // Find a claimed server and install a role first
      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              // Install first role
              val cmd1 = InstallServerRoleCommand(
                playerIndex = 0,
                targetServerName = server.name,
                roleType = blueprint.roleType
              )

              val gameWithRole = cmd1.doStep(game).get

              // Try to install another role
              val blueprints = game.model.roleBlueprints
              if (blueprints.length > 1) {
                val cmd2 = InstallServerRoleCommand(
                  playerIndex = 0,
                  targetServerName = server.name,
                  roleType = blueprints(1).roleType
                )

                val result = cmd2.doStep(gameWithRole)
                result.isFailure shouldBe true
              } else {
                succeed
              }

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "fail if role blueprint not found" in {
      val game = baseGame

      // Find a claimed server
      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          // Use a role type that doesn't exist in blueprints
          val nonExistentRole = ServerRoleType.Inactive

          val hasInactiveBlueprint = game.model.roleBlueprints.exists(_.roleType == nonExistentRole)

          if (!hasInactiveBlueprint) {
            val cmd = InstallServerRoleCommand(
              playerIndex = 0,
              targetServerName = server.name,
              roleType = nonExistentRole
            )

            val result = cmd.doStep(game)
            result.isFailure shouldBe true
          } else {
            succeed
          }

        case None =>
          succeed
      }
    }

    "set correct blocked duration from blueprint" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              val currentRound = game.state.round

              val cmd = InstallServerRoleCommand(
                playerIndex = 0,
                targetServerName = server.name,
                roleType = blueprint.roleType
              )

              val result = cmd.doStep(game).get

              val updatedServer = result.model.servers.find(_.name == server.name).get
              updatedServer.blockedUntilRound shouldBe Some(currentRound + blueprint.setupDurationRounds)

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "undo install role command" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              val cmd = InstallServerRoleCommand(
                playerIndex = 0,
                targetServerName = server.name,
                roleType = blueprint.roleType
              )

              val afterDo = cmd.doStep(game).get
              val afterUndo = cmd.undoStep(afterDo).get

              val revertedServer = afterUndo.model.servers.find(_.name == server.name).get
              revertedServer.installedRole shouldBe None
              revertedServer.blockedUntilRound shouldBe server.blockedUntilRound

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "undo without previous state should return game unchanged" in {
      val game = baseGame

      val cmd = InstallServerRoleCommand(
        playerIndex = 0,
        targetServerName = "any-server",
        roleType = ServerRoleType.BitcoinMiner
      )

      val result = cmd.undoStep(game)
      result.isSuccess shouldBe true
      result.get shouldBe game
    }

    "fail with invalid player index (negative)" in {
      val game = baseGame

      val cmd = InstallServerRoleCommand(
        playerIndex = -1,
        targetServerName = "any-server",
        roleType = ServerRoleType.BitcoinMiner
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "fail with invalid player index (too high)" in {
      val game = baseGame

      val cmd = InstallServerRoleCommand(
        playerIndex = 999,
        targetServerName = "any-server",
        roleType = ServerRoleType.BitcoinMiner
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "correctly set role network range from blueprint" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              val cmd = InstallServerRoleCommand(0, server.name, blueprint.roleType)
              val result = cmd.doStep(game).get

              val updatedServer = result.model.servers.find(_.name == server.name).get
              val role = updatedServer.installedRole.get

              role.networkRange shouldBe blueprint.networkRange

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "preserve server state except for role and block" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              val cmd = InstallServerRoleCommand(0, server.name, blueprint.roleType)
              val result = cmd.doStep(game).get

              val updatedServer = result.model.servers.find(_.name == server.name).get

              updatedServer.name shouldBe server.name
              updatedServer.tile shouldBe server.tile
              updatedServer.difficulty shouldBe server.difficulty
              updatedServer.cybersecurityLevel shouldBe server.cybersecurityLevel
              updatedServer.rewardCpu shouldBe server.rewardCpu
              updatedServer.rewardRam shouldBe server.rewardRam
              updatedServer.hacked shouldBe server.hacked
              updatedServer.claimedBy shouldBe server.claimedBy

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

  }
}
