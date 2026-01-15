package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.util.Observer

/** Interface for the game controller component. Defines all public operations that can be performed
  * on the game state. This interface enforces the component boundary and prevents direct access to
  * internal implementation details.
  */
trait ControllerInterface:

  // Observer pattern methods
  def add(observer: Observer): Unit
  def remove(observer: Observer): Unit

  // Query methods - read-only access to game state
  def game: Game
  def getPlayers: List[Player]
  def getServers: List[Server]
  def getMapData(): Vector[Vector[MapObject]]
  def getState: GameState

  // Command execution
  def doAndRemember(cmd: Command): Unit
  def undo(): Unit
  def redo(): Unit

  // Query command state
  def canUndo: Boolean
  def canRedo: Boolean

  // Game state manipulation
  def advanceRound(): Unit
  def setPhase(newPhase: Phase): Unit
  def setStatus(newStatus: GameStatus): Unit
  def setGame(newGame: Game): Unit

  // File I/O operations
  def save(): Unit
  def load(): Unit
