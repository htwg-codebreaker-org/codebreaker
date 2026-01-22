package de.htwg.codebreaker.controller.commands.server

import scala.util.{Try, Success, Failure}
import de.htwg.codebreaker.controller.Command
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.server.{Server, RunningRoleAction}

case class StartRoleActionCommand(
  playerIndex: Int,
  targetServerName: String,
  actionId: String
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

    if (server.installedRole.isEmpty)
      return Failure(new IllegalArgumentException("Keine Role installiert!"))

    val role = server.installedRole.get

    // Prüfe ob Setup-Zeit abgelaufen ist
    val currentRound = game.state.round
    val roundsLeft = server.blockedUntilRound.getOrElse(currentRound) - currentRound
    
    if (roundsLeft > 0)
      return Failure(new IllegalArgumentException(s"Role noch nicht bereit! Noch $roundsLeft Runden."))

    val actionBlueprint = game.model.actionBlueprints.find(_.id == actionId)
    if (actionBlueprint.isEmpty)
      return Failure(new IllegalArgumentException("Action-Blueprint nicht gefunden"))

    val blueprint = actionBlueprint.get

    if (blueprint.roleType != role.roleType)
      return Failure(new IllegalArgumentException("Action passt nicht zur Role!"))

    val runningAction = RunningRoleAction(
      actionId = actionId,
      startRound = game.state.round,
      completionRound = game.state.round + blueprint.durationRounds,
      detectionIncrease = blueprint.detectionRiskIncrease,
      expectedRewards = blueprint.rewards
    )

    // Aktiviere Role beim ersten Start + entferne Block
    val updatedRole = role.copy(
      isActive = true,  // ← Automatisch aktivieren!
      runningActions = runningAction :: role.runningActions,
      detectionRisk = role.detectionRisk + blueprint.detectionRiskIncrease
    )

    val updatedServer = server.copy(
      installedRole = Some(updatedRole),
      blockedUntilRound = None  // ← Block entfernen
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