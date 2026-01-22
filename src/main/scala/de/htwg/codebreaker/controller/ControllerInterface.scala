package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model.map.{MapObject}
import de.htwg.codebreaker.model.server.Server
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.player.laptop.RunningLaptopAction
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.game.GameModel
import de.htwg.codebreaker.model.game.{GameState, GameStatus, Phase}
import de.htwg.codebreaker.util.Observer

/**
 * Interface for the game controller component.
 * Defines all public operations that can be performed on the game state.
 * This interface enforces the component boundary and prevents direct access
 * to internal implementation details.
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
  def getCompletedActionsForCurrentPlayer(): List[RunningLaptopAction]

  // Command execution
  def doAndRemember(cmd: Command): Unit
  def doAndForget(cmd: Command): Unit
  def undo(): Unit
  def redo(): Unit

  // Query command state
  def canUndo: Boolean
  def canRedo: Boolean

  // Game state manipulation
  def setPhase(newPhase: Phase): Unit
  def setStatus(newStatus: GameStatus): Unit
  def setGame(newGame: Game): Unit

  // File I/O operations
  def save(): Unit
  def load(): Unit
