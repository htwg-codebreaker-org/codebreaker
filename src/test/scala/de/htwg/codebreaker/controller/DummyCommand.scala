package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model.game.game.Game
import scala.util.{Try, Success}

class DummyCommand extends Command:
  
  override def doStep(game: Game): Try[Game] = 
    Success(game.copy(state = game.state.advanceRound()))
  
  override def undoStep(game: Game): Try[Game] = 
    Success(game.copy(state = game.state.copy(round = game.state.round - 1)))
