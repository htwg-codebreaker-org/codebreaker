package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model.game.Game
import scala.util.{Try, Success}

case class NextPlayerCommand() extends Command {

  override def doStep(game: Game): Try[Game] = Try {
    val totalPlayers = game.model.players.length
    val currentIndex = game.state.currentPlayerIndex.getOrElse(0)
    val nextIndex = (currentIndex + 1) % totalPlayers

    val roundInc = if currentIndex == totalPlayers - 1 then 1 else 0
    val newRound = game.state.round + roundInc

    game.copy(
      state = game.state.copy(
        currentPlayerIndex = Some(nextIndex),
        round = newRound
      )
    )
  }

  override def undoStep(game: Game): Try[Game] = Try {
    val totalPlayers = game.model.players.length
    val currentIndex = game.state.currentPlayerIndex.getOrElse(0)
    val prevIndex = if currentIndex == 0 then totalPlayers - 1 else currentIndex - 1

    val roundDec = if currentIndex == 0 then 1 else 0
    val newRound = game.state.round - roundDec

    game.copy(
      state = game.state.copy(
        currentPlayerIndex = Some(prevIndex),
        round = newRound
      )
    )
  }
}