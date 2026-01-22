package de.htwg.codebreaker.controller.commands.player

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.controller._
import scala.util.{Try, Success}

case class NextPlayerCommand() extends Command {

  private var oldPlayerIndex: Option[Int] = None
  private var oldRound: Option[Int] = None
  private var oldMovementPoints: Option[Int] = None

  override def doStep(game: Game): Try[Game] = {
    val totalPlayers = game.model.players.length
    val currentIndex = game.state.currentPlayerIndex.getOrElse(0)

    // Save old state for undo
    oldPlayerIndex = Some(currentIndex)
    oldRound = Some(game.state.round)
    val players = game.model.players
    val nextIndex = (currentIndex + 1) % totalPlayers
    oldMovementPoints = Some(players(nextIndex).movementPoints)

    val roundInc  = if (currentIndex == totalPlayers - 1) 1 else 0
    val newRound  = game.state.round + roundInc

    // Bewegungspunkte für nächsten Spieler
    val nextPlayer  = players(nextIndex)
    val refreshed   = nextPlayer.copy(
      movementPoints = nextPlayer.maxMovementPoints
    )

    Success(game.copy(
      model = game.model.copy(
        players = players.updated(nextIndex, refreshed)
      ),
      state = game.state.copy(
        currentPlayerIndex = Some(nextIndex),
        round = newRound
      )
    ))
  }

  override def undoStep(game: Game): Try[Game] = Try {
    (oldPlayerIndex, oldRound, oldMovementPoints) match {
      case (Some(prevIndex), Some(prevRound), Some(prevMovementPts)) =>
        val currentIndex = game.state.currentPlayerIndex.getOrElse(0)
        val players = game.model.players
        val currentPlayer = players(currentIndex)
        val restoredPlayer = currentPlayer.copy(movementPoints = prevMovementPts)

        game.copy(
          model = game.model.copy(
            players = players.updated(currentIndex, restoredPlayer)
          ),
          state = game.state.copy(
            currentPlayerIndex = Some(prevIndex),
            round = prevRound
          )
        )
      case _ =>
        throw new IllegalStateException("No previous state saved for undo")
    }
  }
}