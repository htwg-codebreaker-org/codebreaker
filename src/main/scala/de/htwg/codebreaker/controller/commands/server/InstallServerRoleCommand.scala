package de.htwg.codebreaker.controller.commands.server

import scala.util.{Try, Success, Failure}
import de.htwg.codebreaker.controller.Command
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.server.{Server, InstalledServerRole, ServerRoleType}

/**
 * Installiert eine Role auf einem geclaimten Server.
 */
case class InstallServerRoleCommand(
  playerIndex: Int,
  targetServerName: String,
  roleType: ServerRoleType
) extends Command {

  private var previousServerState: Option[Server] = None

  override def doStep(game: Game): Try[Game] = {
    val serverOpt = game.model.servers.find(_.name == targetServerName)
    if (serverOpt.isEmpty)
      return Failure(new IllegalArgumentException("Server nicht gefunden"))
    
    val server = serverOpt.get
    previousServerState = Some(server)

    if (!server.claimedBy.contains(playerIndex))
      return Failure(new IllegalArgumentException("Server nicht geclaimed!"))

    if (server.installedRole.isDefined)
      return Failure(new IllegalArgumentException("Server hat bereits eine Role!"))

    // roleBlueprints statt serverRoles
    val roleBlueprint = game.model.roleBlueprints.find(_.roleType == roleType)
    if (roleBlueprint.isEmpty)
      return Failure(new IllegalArgumentException("Role-Blueprint nicht gefunden"))

    val blueprint = roleBlueprint.get

    val installedRole = InstalledServerRole(
      roleType = roleType,
      installStartRound = game.state.round,
      isActive = false,
      detectionRisk = blueprint.baseDetectionRisk,
      runningActions = Nil,
      networkRange = blueprint.networkRange
    )

    val updatedServer = server.copy(
      installedRole = Some(installedRole),
      blockedUntilRound = Some(game.state.round + blueprint.setupDurationRounds)
    )

    val updatedServers = game.model.servers.map {
      case s if s.name == targetServerName => updatedServer
      case s => s
    }

    Success(game.copy(
      model = game.model.copy(servers = updatedServers)
    ))
  }

  override def undoStep(game: Game): Try[Game] = Try {
    previousServerState match {
      case Some(oldServer) =>
        val servers = game.model.servers.map {
          case s if s.name == targetServerName => oldServer
          case s => s
        }
        game.copy(model = game.model.copy(servers = servers))
      case None => game
    }
  }
}