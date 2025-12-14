package de.htwg.codebreaker.controller

import scala.util.{Try, Random}
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
  private var hackWasSuccessful: Boolean = false

  override def doStep(game: Game): Try[Game] = Try {
    val player = game.model.players(playerIndex)
    val server = game.model.servers.find(_.name == serverName)
      .getOrElse(throw new IllegalArgumentException(s"Server '$serverName' nicht gefunden"))

    // Validierungen
    require(player.tile == server.tile,
      s"Spieler muss auf Server-Tile sein (Spieler: ${player.tile}, Server: ${server.tile})")
    require(!server.hacked,
      s"Server '${server.name}' wurde bereits gehackt")
    require(server.serverType != ServerType.Private,
      "Private Server können nicht gehackt werden")

    // Kosten berechnen
    val cpuCost = math.max(1, server.difficulty / 2)
    val ramCost = math.max(1, server.difficulty / 3)

    require(player.cpu >= cpuCost,
      s"Nicht genug CPU (benötigt: $cpuCost, vorhanden: ${player.cpu})")
    require(player.ram >= ramCost,
      s"Nicht genug RAM (benötigt: $ramCost, vorhanden: ${player.ram})")

    // Ressourcen abziehen
    val playerAfterCost = player.copy(
      cpu = player.cpu - cpuCost,
      ram = player.ram - ramCost
    )

    // Erfolgswahrscheinlichkeit berechnen
    val baseChance = 100 - server.difficulty
    val securityBonus = player.cybersecurity / 2
    val successChance = math.max(5, math.min(95, baseChance + securityBonus)) // 5-95%

    val roll = random.nextInt(100)
    hackWasSuccessful = roll < successChance

    if (hackWasSuccessful) {
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
        claimedBy = Some(playerIndex) // Besitz beim Hack setzen
      )

      // State für Undo speichern
      previousPlayerState = Some(player)
      previousServerState = Some(server)

      // Game aktualisieren
      val newPlayers = game.model.players.updated(playerIndex, updatedPlayer)
      val newServers = game.model.servers.map(s =>
        if (s.name == serverName) updatedServer else s
      )

      game.copy(model = game.model.copy(
        players = newPlayers,
        servers = newServers
      ))

    } else {
      // MISSERFOLG - Ressourcen verloren, kein Gewinn
      previousPlayerState = Some(player)
      hackWasSuccessful = false

      val newPlayers = game.model.players.updated(playerIndex, playerAfterCost)
      game.copy(model = game.model.copy(players = newPlayers))
    }
  }

  override def undoStep(game: Game): Try[Game] = Try {
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

        game.copy(model = game.model.copy(
          players = newPlayers,
          servers = newServers
        ))

      case None =>
        throw new IllegalStateException("Kein vorheriger Spieler-Zustand für Undo gespeichert")
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
        // Bank gibt Code statt CPU/RAM
        HackResult(0, 0, server.rewardCpu, 40)
      case ServerType.Military =>
        // Military gibt doppelte Belohnungen
        HackResult(server.rewardCpu * 2, server.rewardRam * 2, 0, 50)
      case ServerType.GKS      =>
        // Endziel - hohe XP-Belohnung
        HackResult(0, 0, 0, 100)
      case ServerType.Private  =>
        // Sollte nicht passieren (wird vorher abgefangen)
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
