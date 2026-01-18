package de.htwg.codebreaker.controller.commands

import scala.util.{Try, Success, Failure, Random}
import de.htwg.codebreaker.controller.Command
import de.htwg.codebreaker.model.server.{Server, ServerType}
import de.htwg.codebreaker.model.player.skill.HackSkill
import de.htwg.codebreaker.model.game.game.Game
import de.htwg.codebreaker.model.player.Player

case class HackServerCommand(
  serverName: String,
  playerIndex: Int,
  skill: HackSkill,
  random: Random = Random()
) extends Command {

  private var previousPlayerState: Option[Player] = None
  private var previousServerState: Option[Server] = None

  override def doStep(game: Game): Try[Game] = {

    // ---- Spieler validieren ----
    if (playerIndex < 0 || playerIndex >= game.model.players.length)
      return Failure(new IllegalArgumentException("Ungültiger Spieler"))

    val player = game.model.players(playerIndex)

    // ---- Server finden ----
    val serverOpt = game.model.servers.find(_.name == serverName)
    if (serverOpt.isEmpty)
      return Failure(new IllegalArgumentException("Server nicht gefunden"))
    
    val server = serverOpt.get

    // ---- Grundvalidierungen ----
    if (player.tile != server.tile)
      return Failure(new IllegalArgumentException("Spieler nicht auf Server-Tile"))

    if (server.hacked)
      return Failure(new IllegalArgumentException("Server bereits gehackt"))

    if (server.serverType == ServerType.Private)
      return Failure(new IllegalArgumentException("Private Server"))

    // ---- Skill validieren ----
    if (!player.skills.unlockedHackSkills.contains(skill.id))
      return Failure(new IllegalArgumentException("Skill nicht freigeschaltet"))

    // ---- Kosten ----
    val cpuCost = math.max(1, server.difficulty / 2)
    val ramCost = math.max(1, server.difficulty / 3)

    if (player.laptop.hardware.cpu < cpuCost || player.laptop.hardware.ram < ramCost)
      return Failure(new IllegalArgumentException("Nicht genug Ressourcen"))

    // ---- Undo-State ----
    previousPlayerState = Some(player)

    val playerAfterCost = player.copy(
      laptop = player.laptop.copy(
        hardware = player.laptop.hardware.copy(
          cpu = player.laptop.hardware.cpu - cpuCost,
          ram = player.laptop.hardware.ram - ramCost,
          code = player.laptop.hardware.code
        )
      )
    )

    // ---- Erfolgschance ----
    val baseChance     = 100 - server.difficulty
    val securityBonus  = player.cybersecurity / 2
    val skillBonus     = skill.successBonus

    val successChance =
      math.max(5, math.min(95, baseChance + securityBonus + skillBonus))

    val success = random.nextInt(100) < successChance

    if (success) {
      val rewards = calculateRewards(server)

      val updatedPlayer = playerAfterCost.copy(
        laptop = playerAfterCost.laptop.copy(
          hardware = playerAfterCost.laptop.hardware.copy(
            cpu = playerAfterCost.laptop.hardware.cpu + rewards.cpuGained,
            ram = playerAfterCost.laptop.hardware.ram + rewards.ramGained,
            code = playerAfterCost.laptop.hardware.code + rewards.codeGained
          )
        ),
        availableXp = playerAfterCost.availableXp + rewards.xpGained,
        totalXpEarned = playerAfterCost.totalXpEarned + rewards.xpGained
      )

      val updatedServer = server.copy(
        hacked = true,
        hackedBy = Some(playerIndex),
        claimedBy = Some(playerIndex)
      )

      previousServerState = Some(server)

      Success(game.copy(
        model = game.model.copy(
          players = game.model.players.updated(playerIndex, updatedPlayer),
          servers = game.model.servers.map {
            case s if s.name == serverName => updatedServer
            case s => s
          }
        )
      ))
    } else {
      Success(game.copy(
        model = game.model.copy(
          players = game.model.players.updated(playerIndex, playerAfterCost)
        )
      ))
    }
  }

  override def undoStep(game: Game): Try[Game] = Try {
    val players = previousPlayerState match {
      case Some(oldPlayer) => game.model.players.updated(playerIndex, oldPlayer)
      case None => game.model.players
    }

    val servers = previousServerState match {
      case Some(oldServer) =>
        game.model.servers.map {
          case s if s.name == serverName => oldServer
          case s => s
        }
      case None => game.model.servers
    }

    game.copy(model = game.model.copy(players = players, servers = servers))
  }

  private def calculateRewards(server: Server): HackResult =
    server.serverType match {
      case ServerType.Side     => HackResult(server.rewardCpu, server.rewardRam, 0, 10)
      case ServerType.Firm     => HackResult(server.rewardCpu, server.rewardRam, 0, 20)
      case ServerType.Cloud    => HackResult(server.rewardCpu, server.rewardRam, 0, 30)
      case ServerType.Bank     => HackResult(0, 0, server.rewardCpu, 40)
      case ServerType.Military => HackResult(server.rewardCpu * 2, server.rewardRam * 2, 0, 50)
      case ServerType.GKS      => HackResult(0, 0, 0, 100)
      case ServerType.Private  => HackResult(0, 0, 0, 0)
    }
}

case class HackResult(
  cpuGained: Int,
  ramGained: Int,
  codeGained: Int,
  xpGained: Int
)
