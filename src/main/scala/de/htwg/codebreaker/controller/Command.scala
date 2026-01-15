package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model.game.Game

import scala.util.Try

/** Funktionales Command Pattern für Undo/Redo. Jeder Command kapselt eine Game-Transformation und
  * deren Undo.
  */
trait Command {
  def doStep(game: Game): Try[Game]
  def undoStep(game: Game): Try[Game]
}
