package de.htwg.codebreaker.controller

import scala.util.{Try, Success, Failure, Random}
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.{Server, ServerType, Player}

/**
 * Command zum Hacken eines Servers durch einen Spieler.
 *
 * Spielmechanik:
 * - Spieler muss auf dem Server-Tile stehen
 * - Kostet CPU und RAM basierend auf Schwierigkeit
 * - Erfolgswahrscheinlichkeit: (100 - difficulty) + (cybersecurity / 2)
 * - Bei Erfolg: Belohnungen und XP, Server wird gehackt und geclaimt
 * - Bei Misserfolg: Ressourcen verloren, kein Gewinn
 *
 * @param serverName Name des zu hackenden Servers
 * @param playerIndex Index des hackenden Spielers
 * @param random Random-Generator (für Testbarkeit)
 */
case class HackServerCommand(
  serverName: String,
  playerIndex: Int,
  random: Random = Random()
) extends Command {

  private var previousPlayerState: Option[Player] = None
  private var previousServerState: Option[Server] = None

  override def doStep(game: Game): Try[Game] = {
    if (playerIndex < 0 || playerIndex >= game.model.players.length) {
      return Failure(new IllegalArgumentException(s"Ungültiger Spieler-Index: $playerIndex"))
    }

    val player = game.model.players(playerIndex)
    
    val serverOpt = game.model.servers.find(_.name == serverName)
    if (serverOpt.isEmpty) {
      return Failure(new IllegalArgumentException(s"Server '$serverName' nicht gefunden"))
    }
    val server = serverOpt.get

    // Validierungen
    if (player.tile != server.tile) {
      return Failure(new IllegalArgumentException(
        s"Spieler muss auf Server-Tile sein (Spieler: ${player.tile}, Server: ${server.tile})"))
    }
    
    if (server.hacked) {
      return Failure(new IllegalArgumentException(s"Server '${server.name}' wurde bereits gehackt"))
    }
    
    if (server.serverType == ServerType.Private) {
      return Failure(new IllegalArgumentException("Private Server können nicht gehackt werden"))
    }

    // Kosten berechnen
    val cpuCost = math.max(1, server.difficulty / 2)
    val ramCost = math.max(1, server.difficulty / 3)

    if (player.cpu < cpuCost) {
      return Failure(new IllegalArgumentException(
        s"Nicht genug CPU (benötigt: $cpuCost, vorhanden: ${player.cpu})"))
    }
    
    if (player.ram < ramCost) {
      return Failure(new IllegalArgumentException(
        s"Nicht genug RAM (benötigt: $ramCost, vorhanden: ${player.ram})"))
    }

    // State für Undo speichern
    previousPlayerState = Some(player)

    // Ressourcen abziehen
    val playerAfterCost = player.copy(
      cpu = player.cpu - cpuCost,
      ram = player.ram - ramCost
    )

    // Erfolgswahrscheinlichkeit berechnen
    val baseChance = 100 - server.difficulty
    val securityBonus = player.cybersecurity / 2
    val successChance = math.max(5, math.min(95, baseChance + securityBonus))

    val roll = random.nextInt(100)
    val hackSuccessful = roll < successChance

    if (hackSuccessful) {
      // ERFOLG! Belohnungen berechnen
      val rewards = calculateRewards(server)

      val updatedPlayer = playerAfterCost.copy(
        cpu = playerAfterCost.cpu + rewards.cpuGained,
        ram = playerAfterCost.ram + rewards.ramGained,
        code = playerAfterCost.code + rewards.codeGained,
        xp = playerAfterCost.xp + rewards.xpGained
      )

      val updatedServer = server.copy(
        hacked = true,
        hackedBy = Some(playerIndex),
        claimedBy = Some(playerIndex)
      )

      // Server State für Undo speichern
      previousServerState = Some(server)

      // Game aktualisieren
      val newPlayers = game.model.players.updated(playerIndex, updatedPlayer)
      val newServers = game.model.servers.map(s =>
        if (s.name == serverName) updatedServer else s
      )

      Success(game.copy(model = game.model.copy(
        players = newPlayers,
        servers = newServers
      )))

    } else {
      // MISSERFOLG - nur Ressourcen verloren
      val newPlayers = game.model.players.updated(playerIndex, playerAfterCost)
      Success(game.copy(model = game.model.copy(players = newPlayers)))
    }
  }

  override def undoStep(game: Game): Try[Game] = {
    previousPlayerState match {
      case Some(oldPlayer) =>
        val newPlayers = game.model.players.updated(playerIndex, oldPlayer)

        val newServers = previousServerState match {
          case Some(oldServer) =>
            // Hack war erfolgreich - Server zurücksetzen
            game.model.servers.map(s =>
              if (s.name == serverName) oldServer else s
            )
          case None =>
            // Hack war nicht erfolgreich - Server unverändert
            game.model.servers
        }

        Success(game.copy(model = game.model.copy(
          players = newPlayers,
          servers = newServers
        )))

      case None =>
        Failure(new IllegalStateException("Kein vorheriger Spieler-Zustand für Undo gespeichert"))
    }
  }

  /**
   * Berechnet Belohnungen basierend auf Server-Typ
   */
  private def calculateRewards(server: Server): HackResult = {
    server.serverType match {
      case ServerType.Side     =>
        HackResult(server.rewardCpu, server.rewardRam, 0, 10)
      case ServerType.Firm     =>
        HackResult(server.rewardCpu, server.rewardRam, 0, 20)
      case ServerType.Cloud    =>
        HackResult(server.rewardCpu, server.rewardRam, 0, 30)
      case ServerType.Bank     =>
        HackResult(0, 0, server.rewardCpu, 40)
      case ServerType.Military =>
        HackResult(server.rewardCpu * 2, server.rewardRam * 2, 0, 50)
      case ServerType.GKS      =>
        HackResult(0, 0, 0, 100)
      case ServerType.Private  =>
        HackResult(0, 0, 0, 0)
    }
  }
}

/**
 * Ergebnis eines Hack-Versuchs
 */
case class HackResult(
  cpuGained: Int,
  ramGained: Int,
  codeGained: Int,
  xpGained: Int
)