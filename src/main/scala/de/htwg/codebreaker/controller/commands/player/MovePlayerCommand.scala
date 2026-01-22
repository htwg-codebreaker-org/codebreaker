package de.htwg.codebreaker.controller.commands.player

import scala.util.{Try, Success, Failure}
import de.htwg.codebreaker.controller.Command
import de.htwg.codebreaker.model.map.Tile
import de.htwg.codebreaker.model.game.Game
  

/**
 * Command zum Bewegen eines Spielers zu einem neuen Tile.
 *
 * Bewegungskosten basieren auf Manhattan-Distanz:
 * - Jedes Feld kostet 1 Bewegungspunkt
 * - Spieler braucht genug movementPoints
 *
 * @param playerIndex Index des Spielers, der bewegt werden soll
 * @param newTile Das Ziel-Tile
 */
case class MovePlayerCommand(playerIndex: Int, newTile: Tile) extends Command {

  private var oldTile: Option[Tile] = None
  private var oldMovementPoints: Option[Int] = None

  /**
   * Berechnet Manhattan-Distanz zwischen zwei Tiles
   */
  private def calculateDistance(from: Tile, to: Tile): Int = {
    math.abs(to.x - from.x) + math.abs(to.y - from.y)
  }

  override def doStep(game: Game): Try[Game] = {
    val players = game.model.players

    // Validierung: Spieler-Index existiert?
    if (playerIndex < 0 || playerIndex >= players.length) {
      return Failure(new IllegalArgumentException(s"Ungültiger Spieler-Index: $playerIndex"))
    }

    val player = players(playerIndex)

    // Validierung: Ziel-Tile ist Land (nicht Ocean)?
    if (!newTile.continent.isLand) {
      return Failure(new IllegalArgumentException(
        s"Kann nicht auf Ocean-Tile bei (${newTile.x}, ${newTile.y}) bewegen"))
    }

    // Distanz berechnen
    val distance = calculateDistance(player.tile, newTile)

    if (distance <= 0) {
      return Failure(new IllegalArgumentException(
        s"Neues Tile muss sich von aktuellem Tile unterscheiden"))
    }

    // Validierung: Genug Bewegungspunkte?
    if (player.movementPoints < distance) {
      return Failure(new IllegalArgumentException(
        s"Nicht genug Bewegungspunkte (benötigt: $distance, vorhanden: ${player.movementPoints})"))
    }

    // Altes Tile und Bewegungspunkte für Undo speichern
    oldTile = Some(player.tile)
    oldMovementPoints = Some(player.movementPoints)

    // Spieler mit neuem Tile und reduzierten Bewegungspunkten aktualisieren
    val movedPlayer = player.copy(
      tile = newTile,
      movementPoints = player.movementPoints - distance
    )

    val updatedPlayers = players.updated(playerIndex, movedPlayer)
    Success(game.copy(model = game.model.copy(players = updatedPlayers)))
  }

  override def undoStep(game: Game): Try[Game] = Try {
    (oldTile, oldMovementPoints) match {
      case (Some(tile), Some(points)) =>
        val players = game.model.players
        val player = players(playerIndex)
        val revertedPlayer = player.copy(
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