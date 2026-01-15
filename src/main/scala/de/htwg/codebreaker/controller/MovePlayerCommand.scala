package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.{Player, ServerType, Tile}

import scala.util.{Failure, Random, Success, Try}

/** Command zum Bewegen eines Spielers zu einem neuen Tile.
  *
  * Bewegungskosten basieren auf Manhattan-Distanz:
  *   - Jedes Feld kostet 1 Bewegungspunkt
  *   - Spieler braucht genug movementPoints
  *
  * @param playerIndex
  *   Index des Spielers, der bewegt werden soll
  * @param newTile
  *   Das Ziel-Tile
  */
case class MovePlayerCommand(playerIndex: Int, newTile: Tile, random: Random = Random())
    extends Command {

  private var oldTile: Option[Tile]                                    = None
  private var oldMovementPoints: Option[Int]                           = None
  private var oldPlayerState: Option[Player]                           = None
  private var oldServerState: Option[de.htwg.codebreaker.model.Server] = None
  private var hackWasAttempted: Boolean                                = false

  /** Berechnet Manhattan-Distanz zwischen zwei Tiles
    */
  private def calculateDistance(from: Tile, to: Tile): Int =
    math.abs(to.x - from.x) + math.abs(to.y - from.y)

  override def doStep(game: Game): Try[Game] = Try {
    val players = game.model.players

    // Validierung: Spieler-Index existiert?
    require(
      playerIndex >= 0 && playerIndex < players.length,
      s"Ungültiger Spieler-Index: $playerIndex"
    )

    val player = players(playerIndex)

    // Validierung: Ziel-Tile ist Land (nicht Ocean)?
    require(
      newTile.continent.isLand,
      s"Kann nicht auf Ocean-Tile bei (${newTile.x}, ${newTile.y}) bewegen"
    )

    // Distanz berechnen
    val distance = calculateDistance(player.tile, newTile)

    // Validierung: Genug Bewegungspunkte?
    require(
      player.movementPoints >= distance,
      s"Nicht genug Bewegungspunkte (benötigt: $distance, vorhanden: ${player.movementPoints})"
    )

    // Altes Tile und Bewegungspunkte für Undo speichern
    oldTile = Some(player.tile)
    oldMovementPoints = Some(player.movementPoints)

    // Spieler mit neuem Tile und reduzierten Bewegungspunkten aktualisieren
    val movedPlayer = player.copy(
      tile = newTile,
      movementPoints = player.movementPoints - distance
    )

    // Prüfen ob auf dem Ziel-Tile ein Server ist
    val serverOpt = game.model.servers.find(_.tile == newTile)

    serverOpt match {
      case Some(server) if !server.hacked && server.serverType != ServerType.Private =>
        // Server gefunden und noch nicht gehackt - automatischer Hack-Versuch
        hackWasAttempted = true
        oldPlayerState = Some(player)
        oldServerState = Some(server)

        // Hack-Logik (analog zu HackServerCommand)
        val cpuCost = math.max(1, server.difficulty / 2)
        val ramCost = math.max(1, server.difficulty / 3)

        if (movedPlayer.cpu >= cpuCost && movedPlayer.ram >= ramCost) {
          // Genug Ressourcen - Hack-Versuch durchführen
          val playerAfterCost = movedPlayer.copy(
            cpu = movedPlayer.cpu - cpuCost,
            ram = movedPlayer.ram - ramCost
          )

          // Erfolgswahrscheinlichkeit berechnen
          val baseChance    = 100 - server.difficulty
          val securityBonus = movedPlayer.cybersecurity / 2
          val successChance = math.max(5, math.min(95, baseChance + securityBonus))

          val roll           = random.nextInt(100)
          val hackSuccessful = roll < successChance

          if (hackSuccessful) {
            // Erfolg! Belohnungen gewähren
            val (cpuReward, ramReward, codeReward, xpReward) = server.serverType match {
              case ServerType.Side     => (server.rewardCpu, server.rewardRam, 0, 10)
              case ServerType.Firm     => (server.rewardCpu, server.rewardRam, 0, 20)
              case ServerType.Cloud    => (server.rewardCpu, server.rewardRam, 0, 30)
              case ServerType.Bank     => (0, 0, server.rewardCpu, 40)
              case ServerType.Military => (server.rewardCpu * 2, server.rewardRam * 2, 0, 50)
              case ServerType.GKS      => (0, 0, 0, 100)
              case _                   => (0, 0, 0, 0)
            }

            val finalPlayer = playerAfterCost.copy(
              cpu = playerAfterCost.cpu + cpuReward,
              ram = playerAfterCost.ram + ramReward,
              code = playerAfterCost.code + codeReward,
              xp = playerAfterCost.xp + xpReward
            )

            val hackedServer = server.copy(
              hacked = true,
              hackedBy = Some(playerIndex),
              claimedBy = Some(playerIndex)
            )

            val updatedPlayers = players.updated(playerIndex, finalPlayer)
            val updatedServers =
              game.model.servers.map(s => if (s.name == server.name) hackedServer else s)

            game.copy(model =
              game.model.copy(
                players = updatedPlayers,
                servers = updatedServers
              )
            )
          } else {
            // Misserfolg - nur Ressourcen verloren
            val updatedPlayers = players.updated(playerIndex, playerAfterCost)
            game.copy(model = game.model.copy(players = updatedPlayers))
          }
        } else {
          // Nicht genug Ressourcen für Hack - nur bewegen
          val updatedPlayers = players.updated(playerIndex, movedPlayer)
          game.copy(model = game.model.copy(players = updatedPlayers))
        }

      case _ =>
        // Kein Server oder bereits gehackt - nur bewegen
        val updatedPlayers = players.updated(playerIndex, movedPlayer)
        game.copy(model = game.model.copy(players = updatedPlayers))
    }
  }

  override def undoStep(game: Game): Try[Game] = Try {
    (oldTile, oldMovementPoints, oldPlayerState) match {
      case (Some(tile), Some(points), Some(originalPlayer)) if hackWasAttempted =>
        // Hack wurde versucht - kompletten Spieler-Zustand wiederherstellen
        val revertedPlayers = game.model.players.updated(playerIndex, originalPlayer)

        // Server zurücksetzen falls gehackt
        val revertedServers = oldServerState match {
          case Some(originalServer) =>
            game.model.servers.map(s => if (s.tile == originalServer.tile) originalServer else s)
          case None                 =>
            game.model.servers
        }

        game.copy(model =
          game.model.copy(
            players = revertedPlayers,
            servers = revertedServers
          )
        )

      case (Some(tile), Some(points), _) =>
        // Nur Bewegung ohne Hack - simple Wiederherstellung
        val players         = game.model.players
        val player          = players(playerIndex)
        val revertedPlayer  = player.copy(
          tile = tile,
          movementPoints = points
        )
        val revertedPlayers = players.updated(playerIndex, revertedPlayer)
        game.copy(model = game.model.copy(players = revertedPlayers))

      case _ =>
        throw new IllegalStateException("Kein vorheriger Zustand für Undo gespeichert")
    }
  }
}
