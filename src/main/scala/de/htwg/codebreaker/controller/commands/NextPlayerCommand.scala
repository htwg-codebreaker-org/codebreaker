package de.htwg.codebreaker.controller.commands

import de.htwg.codebreaker.model.game.game.Game
import de.htwg.codebreaker.controller._
import scala.util.{Try, Success}

case class NextPlayerCommand() extends Command {

  override def doStep(game: Game): Try[Game] = {
    val totalPlayers = game.model.players.length
    val currentIndex = game.state.currentPlayerIndex.getOrElse(0)

    val nextIndex = (currentIndex + 1) % totalPlayers
    val roundInc  = if (currentIndex == totalPlayers - 1) 1 else 0
    val newRound  = game.state.round + roundInc

    // 1️⃣ Laptop-Actions verarbeiten (nur Kerne freigeben!)
    ProcessLaptopActionsCommand(newRound).doStep(game).map { processedGame =>

      // 2️⃣ Bewegungspunkte für nächsten Spieler
      val players     = processedGame.model.players
      val nextPlayer  = players(nextIndex)
      val refreshed   = nextPlayer.copy(
        movementPoints = nextPlayer.maxMovementPoints
      )

      processedGame.copy(
        model = processedGame.model.copy(
          players = players.updated(nextIndex, refreshed)
        ),
        state = processedGame.state.copy(
          currentPlayerIndex = Some(nextIndex),
          round = newRound
        )
      )
    }
  }

  override def undoStep(game: Game): Try[Game] =
    Success(game)
}