package de.htwg.codebreaker.controller.commands.server

import scala.util.{Try, Success, Failure, Random}
import de.htwg.codebreaker.controller.Command
import de.htwg.codebreaker.model.game.game.Game
import de.htwg.codebreaker.model.server.Server
import de.htwg.codebreaker.model.player.Player

case class CollectRoleActionCommand(
  playerIndex: Int,
  targetServerName: String,
  actionId: String,
  random: Random = Random()
) extends Command {

  private var previousPlayerState: Option[Player] = None
  private var previousServerState: Option[Server] = None

  override def doStep(game: Game): Try[Game] = {
    val player = game.model.players(playerIndex)
    previousPlayerState = Some(player)

    val serverOpt = game.model.servers.find(_.name == targetServerName)
    if (serverOpt.isEmpty)
      return Failure(new IllegalArgumentException("Server nicht gefunden"))
    
    val server = serverOpt.get
    previousServerState = Some(server)

    if (server.installedRole.isEmpty)
      return Failure(new IllegalArgumentException("Keine Role installiert!"))

    val role = server.installedRole.get

    // Finde fertige Action
    val completedActionOpt = role.runningActions.find { running =>
      running.actionId == actionId && running.completionRound <= game.state.round
    }

    if (completedActionOpt.isEmpty)
      return Failure(new IllegalArgumentException("Keine fertige Action gefunden!"))

    val completedAction = completedActionOpt.get

    // Detection Check
    val detectionChance = role.detectionRisk
    val detected = random.nextInt(100) < detectionChance

    if (detected) {
      // DETECTED! Role zerstört, Server geblockt
      val updatedServer = server.copy(
        installedRole = None,
        blockedUntilRound = Some(game.state.round + 5)
      )

      val updatedServers = game.model.servers.map {
        case s if s.name == targetServerName => updatedServer
        case s => s
      }

      return Success(game.copy(
        model = game.model.copy(servers = updatedServers)
      ))
    }

    // Erfolgreich! Rewards einsammeln
    val rewards = completedAction.expectedRewards
    
    val updatedLaptop = player.laptop.copy(
      hardware = player.laptop.hardware.copy(
        cpu = player.laptop.hardware.cpu + rewards.cpu,
        ram = player.laptop.hardware.ram + rewards.ram,
        code = player.laptop.hardware.code + rewards.code
      )
    )

    val updatedPlayer = player.copy(laptop = updatedLaptop)

    // ✅ FIX: Nur DIE EINE Action entfernen, nicht alle!
    val updatedRole = role.copy(
      networkRange = role.networkRange + rewards.networkRangeBonus,
      runningActions = role.runningActions.filterNot(a =>
        a.actionId == actionId && a.completionRound == completedAction.completionRound
      )
    )


    val updatedServer = server.copy(installedRole = Some(updatedRole))

    val updatedServers = game.model.servers.map {
      case s if s.name == targetServerName => updatedServer
      case s => s
    }

    Success(game.copy(
      model = game.model.copy(
        players = game.model.players.updated(playerIndex, updatedPlayer),
        servers = updatedServers
      )
    ))
  }

  override def undoStep(game: Game): Try[Game] = Try {
    val players = previousPlayerState match {
      case Some(oldPlayer) => game.model.players.updated(playerIndex, oldPlayer)
      case None => game.model.players
    }

    val servers = previousServerState match {
      case Some(oldServer) =>
        game.model.servers.map {
          case s if s.name == targetServerName => oldServer
          case s => s
        }
      case None => game.model.servers
    }

    game.copy(model = game.model.copy(players = players, servers = servers))
  }
}