package de.htwg.codebreaker.controller

import scala.util.{Try, Success, Failure}
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.{Player, Tile}

/**
 * Command zum Bewegen eines Spielers zu einem neuen Tile.
 *
 * @param playerIndex Index des Spielers, der bewegt werden soll
 * @param newTile Das Ziel-Tile
 */
case class MovePlayerCommand(playerIndex: Int, newTile: Tile) extends Command {

  private var oldTile: Option[Tile] = None

  override def doStep(game: Game): Try[Game] = Try {
    val players = game.model.players

    // Validierung: Spieler-Index existiert?
    if (playerIndex < 0 || playerIndex >= players.length) {
      throw new IllegalArgumentException(s"Ungültiger Spieler-Index: $playerIndex")
    }

    val player = players(playerIndex)

    // Validierung: Ziel-Tile ist Land (nicht Ocean)?
    if (!newTile.continent.isLand) {
      throw new IllegalArgumentException(s"Kann nicht auf Ocean-Tile bei (${newTile.x}, ${newTile.y}) bewegen")
    }

    // Altes Tile für Undo speichern
    oldTile = Some(player.tile)

    // Spieler mit neuem Tile aktualisieren
    val updatedPlayer = player.copy(tile = newTile)
    val updatedPlayers = players.updated(playerIndex, updatedPlayer)

    // Neues Game mit aktualisierten Spielern
    val updatedModel = game.model.copy(players = updatedPlayers)
    game.copy(model = updatedModel)
  }

  override def undoStep(game: Game): Try[Game] = Try {
    oldTile match {
      case Some(tile) =>
        val players = game.model.players
        val player = players(playerIndex)
        val revertedPlayer = player.copy(tile = tile)
        val revertedPlayers = players.updated(playerIndex, revertedPlayer)
        val revertedModel = game.model.copy(players = revertedPlayers)
        game.copy(model = revertedModel)
      case None =>
        throw new IllegalStateException("Kein altes Tile gespeichert für Undo")
    }
  }
}
