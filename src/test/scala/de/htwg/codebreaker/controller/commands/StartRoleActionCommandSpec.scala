package de.htwg.codebreaker.controller.commands

import scala.util.{Success, Failure}

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.server.{ServerRoleType, InstalledServerRole}
import de.htwg.codebreaker.controller.commands.server.{InstallServerRoleCommand, StartRoleActionCommand}

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


class StartRoleActionCommandSpec extends CommandTestBase {

  "StartRoleActionCommand" should {

    "start a role action on a server with installed role" in {
      val game = baseGame

      // Find a claimed server and install a role
      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              // Install role
              val installCmd = InstallServerRoleCommand(0, server.name, blueprint.roleType)
              val gameWithRole = installCmd.doStep(game).get

              // Advance rounds to finish setup
              val readyGame = gameWithRole.copy(
                state = gameWithRole.state.copy(
                  round = gameWithRole.state.round + blueprint.setupDurationRounds
                )
              )

              // Find an action blueprint for this role type
              val actionBlueprintOpt = readyGame.model.actionBlueprints.find(
                _.roleType == blueprint.roleType
              )

              actionBlueprintOpt match {
                case Some(actionBlueprint) =>
                  val cmd = StartRoleActionCommand(
                    playerIndex = 0,
                    targetServerName = server.name,
                    actionId = actionBlueprint.id
                  )

                  val result = cmd.doStep(readyGame).get

                  val updatedServer = result.model.servers.find(_.name == server.name).get
                  val role = updatedServer.installedRole.get

                  // Role should be active after first action
                  role.isActive shouldBe true

                  // Running action should be added
                  role.runningActions should have size 1
                  val runningAction = role.runningActions.head
                  runningAction.actionId shouldBe actionBlueprint.id
                  runningAction.startRound shouldBe readyGame.state.round
                  runningAction.completionRound shouldBe (readyGame.state.round + actionBlueprint.durationRounds)

                  // Detection risk should increase
                  role.detectionRisk should be >= blueprint.baseDetectionRisk

                  // Block should be removed
                  updatedServer.blockedUntilRound shouldBe None

                case None =>
                  succeed
              }

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "fail if server not found" in {
      val game = baseGame

      val cmd = StartRoleActionCommand(
        playerIndex = 0,
        targetServerName = "nonexistent-server",
        actionId = "some-action"
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "fail if server not claimed by player" in {
      val game = baseGame

      val unclaimedServerOpt = game.model.servers.find(s =>
        !s.claimedBy.contains(0)
      )

      unclaimedServerOpt match {
        case Some(server) =>
          val cmd = StartRoleActionCommand(
            playerIndex = 0,
            targetServerName = server.name,
            actionId = "some-action"
          )

          val result = cmd.doStep(game)
          result.isFailure shouldBe true

        case None =>
          succeed
      }
    }

    "fail if no role installed" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val cmd = StartRoleActionCommand(
            playerIndex = 0,
            targetServerName = server.name,
            actionId = "some-action"
          )

          val result = cmd.doStep(game)
          result.isFailure shouldBe true

        case None =>
          succeed
      }
    }

    "fail if role not ready (still in setup)" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              // Install role but don't advance rounds
              val installCmd = InstallServerRoleCommand(0, server.name, blueprint.roleType)
              val gameWithRole = installCmd.doStep(game).get

              val actionBlueprintOpt = gameWithRole.model.actionBlueprints.find(
                _.roleType == blueprint.roleType
              )

              actionBlueprintOpt match {
                case Some(actionBlueprint) =>
                  val cmd = StartRoleActionCommand(
                    playerIndex = 0,
                    targetServerName = server.name,
                    actionId = actionBlueprint.id
                  )

                  val result = cmd.doStep(gameWithRole)
                  result.isFailure shouldBe true

                case None =>
                  succeed
              }

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "fail if action blueprint not found" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              val installCmd = InstallServerRoleCommand(0, server.name, blueprint.roleType)
              val gameWithRole = installCmd.doStep(game).get

              // Advance rounds
              val readyGame = gameWithRole.copy(
                state = gameWithRole.state.copy(
                  round = gameWithRole.state.round + blueprint.setupDurationRounds
                )
              )

              val cmd = StartRoleActionCommand(
                playerIndex = 0,
                targetServerName = server.name,
                actionId = "nonexistent-action-id"
              )

              val result = cmd.doStep(readyGame)
              result.isFailure shouldBe true

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "fail if action doesn't match role type" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              val installCmd = InstallServerRoleCommand(0, server.name, blueprint.roleType)
              val gameWithRole = installCmd.doStep(game).get

              // Advance rounds
              val readyGame = gameWithRole.copy(
                state = gameWithRole.state.copy(
                  round = gameWithRole.state.round + blueprint.setupDurationRounds
                )
              )

              // Find an action for a different role type
              val wrongActionOpt = readyGame.model.actionBlueprints.find(
                _.roleType != blueprint.roleType
              )

              wrongActionOpt match {
                case Some(wrongAction) =>
                  val cmd = StartRoleActionCommand(
                    playerIndex = 0,
                    targetServerName = server.name,
                    actionId = wrongAction.id
                  )

                  val result = cmd.doStep(readyGame)
                  result.isFailure shouldBe true

                case None =>
                  succeed
              }

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "increase detection risk when starting action" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              val installCmd = InstallServerRoleCommand(0, server.name, blueprint.roleType)
              val gameWithRole = installCmd.doStep(game).get

              val readyGame = gameWithRole.copy(
                state = gameWithRole.state.copy(
                  round = gameWithRole.state.round + blueprint.setupDurationRounds
                )
              )

              val actionBlueprintOpt = readyGame.model.actionBlueprints.find(
                _.roleType == blueprint.roleType
              )

              actionBlueprintOpt match {
                case Some(actionBlueprint) =>
                  val initialRisk = blueprint.baseDetectionRisk

                  val cmd = StartRoleActionCommand(0, server.name, actionBlueprint.id)
                  val result = cmd.doStep(readyGame).get

                  val updatedServer = result.model.servers.find(_.name == server.name).get
                  val role = updatedServer.installedRole.get

                  role.detectionRisk shouldBe (initialRisk + actionBlueprint.detectionRiskIncrease)

                case None =>
                  succeed
              }

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "undo start role action command" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              val installCmd = InstallServerRoleCommand(0, server.name, blueprint.roleType)
              val gameWithRole = installCmd.doStep(game).get

              val readyGame = gameWithRole.copy(
                state = gameWithRole.state.copy(
                  round = gameWithRole.state.round + blueprint.setupDurationRounds
                )
              )

              val actionBlueprintOpt = readyGame.model.actionBlueprints.find(
                _.roleType == blueprint.roleType
              )

              actionBlueprintOpt match {
                case Some(actionBlueprint) =>
                  val cmd = StartRoleActionCommand(0, server.name, actionBlueprint.id)
                  val afterDo = cmd.doStep(readyGame).get
                  val afterUndo = cmd.undoStep(afterDo).get

                  val revertedServer = afterUndo.model.servers.find(_.name == server.name).get
                  val role = revertedServer.installedRole.get

                  // Role should be back to inactive
                  role.isActive shouldBe false
                  role.runningActions shouldBe Nil

                case None =>
                  succeed
              }

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "undo without previous state should return game unchanged" in {
      val game = baseGame

      val cmd = StartRoleActionCommand(0, "any-server", "any-action")
      val result = cmd.undoStep(game)

      result.isSuccess shouldBe true
      result.get shouldBe game
    }

    "fail with invalid player index (negative)" in {
      val game = baseGame

      val cmd = StartRoleActionCommand(-1, "any-server", "any-action")
      val result = cmd.doStep(game)

      result.isFailure shouldBe true
    }

    "fail with invalid player index (too high)" in {
      val game = baseGame

      val cmd = StartRoleActionCommand(999, "any-server", "any-action")
      val result = cmd.doStep(game)

      result.isFailure shouldBe true
    }

    "activate role on first action start" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              val installCmd = InstallServerRoleCommand(0, server.name, blueprint.roleType)
              val gameWithRole = installCmd.doStep(game).get

              val readyGame = gameWithRole.copy(
                state = gameWithRole.state.copy(
                  round = gameWithRole.state.round + blueprint.setupDurationRounds
                )
              )

              val serverBefore = readyGame.model.servers.find(_.name == server.name).get
              serverBefore.installedRole.get.isActive shouldBe false

              val actionBlueprintOpt = readyGame.model.actionBlueprints.find(
                _.roleType == blueprint.roleType
              )

              actionBlueprintOpt match {
                case Some(actionBlueprint) =>
                  val cmd = StartRoleActionCommand(0, server.name, actionBlueprint.id)
                  val result = cmd.doStep(readyGame).get

                  val serverAfter = result.model.servers.find(_.name == server.name).get
                  serverAfter.installedRole.get.isActive shouldBe true

                case None =>
                  succeed
              }

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

    "set correct completion round for action" in {
      val game = baseGame

      val claimedServerOpt = game.model.servers.find(s =>
        s.claimedBy.contains(0) && s.installedRole.isEmpty
      )

      claimedServerOpt match {
        case Some(server) =>
          val roleBlueprintOpt = game.model.roleBlueprints.headOption

          roleBlueprintOpt match {
            case Some(blueprint) =>
              val installCmd = InstallServerRoleCommand(0, server.name, blueprint.roleType)
              val gameWithRole = installCmd.doStep(game).get

              val readyGame = gameWithRole.copy(
                state = gameWithRole.state.copy(
                  round = gameWithRole.state.round + blueprint.setupDurationRounds
                )
              )

              val actionBlueprintOpt = readyGame.model.actionBlueprints.find(
                _.roleType == blueprint.roleType
              )

              actionBlueprintOpt match {
                case Some(actionBlueprint) =>
                  val currentRound = readyGame.state.round

                  val cmd = StartRoleActionCommand(0, server.name, actionBlueprint.id)
                  val result = cmd.doStep(readyGame).get

                  val updatedServer = result.model.servers.find(_.name == server.name).get
                  val runningAction = updatedServer.installedRole.get.runningActions.head

                  runningAction.startRound shouldBe currentRound
                  runningAction.completionRound shouldBe (currentRound + actionBlueprint.durationRounds)

                case None =>
                  succeed
              }

            case None =>
              succeed
          }

        case None =>
          succeed
      }
    }

  }
}
