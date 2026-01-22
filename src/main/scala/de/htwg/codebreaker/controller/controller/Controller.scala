package de.htwg.codebreaker.controller.controller

import com.google.inject.Inject
import de.htwg.codebreaker.util.Observable
import de.htwg.codebreaker.model.map.{WorldMap, MapObject}
import de.htwg.codebreaker.model.server.Server
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.game.game.{Game, GameState, Phase, GameStatus}
import de.htwg.codebreaker.model.player.laptop.RunningLaptopAction

import de.htwg.codebreaker.persistence.FileIOInterface
import scala.util.{Try, Success, Failure}
import de.htwg.codebreaker.controller._

/**
 * Implementation of the game controller component.
 * Manages game state, command execution, undo/redo functionality, and observer notifications.
 * This class encapsulates all game logic and state management, exposing only the interface methods.
 *
 * @param initialGame The initial game state, injected by Guice
 * @param fileIO The file I/O implementation, injected by Guice
 */
class Controller @Inject() (initialGame: Game, fileIO: FileIOInterface) extends ControllerInterface with Observable:

  private case class ControllerState(
    currentGame: Game,
    undoStack: List[HistoryEntry] = Nil,
    redoStack: List[HistoryEntry] = Nil
  )

  private case class HistoryEntry(gameBefore: Game, command: Command)

  private var state: ControllerState = ControllerState(initialGame)

  def game: Game = state.currentGame

  def canUndo: Boolean = state.undoStack.nonEmpty
  def canRedo: Boolean = state.redoStack.nonEmpty

  def getPlayers: List[Player] = game.model.players
  def getServers: List[Server] = game.model.servers
  def getMapData(): Vector[Vector[MapObject]] =
    game.model.map.getMapData(game.model.players, game.model.servers)
  def getState: GameState = game.state

  def getCompletedActionsForCurrentPlayer(): List[RunningLaptopAction] = {
    game.state.currentPlayerIndex match {
      case Some(idx) if idx >= 0 && idx < game.model.players.length =>
        val player = game.model.players(idx)
        player.laptop.runningActions.filter(_.completionRound <= game.state.round)
      case _ => Nil
    }
  }


  /** Führt ein Command aus und speichert den alten Zustand für Undo. */
  def doAndRemember(cmd: Command): Unit = {
    cmd.doStep(state.currentGame) match {
      case Success(newGame) =>
        state = state.copy(
          currentGame = newGame,
          undoStack = HistoryEntry(state.currentGame, cmd) :: state.undoStack,
          redoStack = Nil
        )
        save()
        notifyObservers
      case Failure(ex) =>
        println(s"Command fehlgeschlagen: ${ex.getMessage}")
    }
  }


  /** Führt ein Command aus und löscht die Undo/Redo-History (für Spieler-/Rundenwechsel). */
  def doAndForget(cmd: Command): Unit = {
    cmd.doStep(state.currentGame) match {
      case Success(newGame) =>
        state = state.copy(
          currentGame = newGame,
          undoStack = Nil,
          redoStack = Nil
        )
        save()
        notifyObservers
      case Failure(ex) =>
        println(s"Command fehlgeschlagen: ${ex.getMessage}")
    }
  }

  /** Macht den letzten Schritt rückgängig. */
  def undo(): Unit = state.undoStack match {
    case HistoryEntry(before, cmd) :: rest =>
      cmd.undoStep(state.currentGame) match {
        case Success(revertedGame) =>
          state = state.copy(
            currentGame = revertedGame,
            undoStack = rest,
            redoStack = HistoryEntry(state.currentGame, cmd) :: state.redoStack
          )
          notifyObservers
        case Failure(ex) =>
          println(s"Undo fehlgeschlagen: ${ex.getMessage}")
      }
    case Nil => println("Nichts zum Rückgängig machen.")
  }


  /** Stellt einen rückgängig gemachten Schritt wieder her. */
  def redo(): Unit = state.redoStack match {
    case HistoryEntry(before, cmd) :: rest =>
      cmd.doStep(state.currentGame) match {
        case Success(redoneGame) =>
          state = state.copy(
            currentGame = redoneGame,
            undoStack = HistoryEntry(state.currentGame, cmd) :: state.undoStack,
            redoStack = rest
          )
          notifyObservers
        case Failure(ex) =>
          println(s"Redo fehlgeschlagen: ${ex.getMessage}")
      }
    case Nil => println("Nichts zum Wiederholen.")
  }

  def setPhase(newPhase: Phase): Unit = {
    state = state.copy(
      currentGame = state.currentGame.copy(
        state = state.currentGame.state.setPhase(newPhase)
      )
    )
    notifyObservers
  }

  def setStatus(newStatus: GameStatus): Unit = {
    state = state.copy(
      currentGame = state.currentGame.copy(
        state = state.currentGame.state.setStatus(newStatus)
      )
    )
    notifyObservers
  }

  def setGame(newGame: Game): Unit = {
    state = ControllerState(newGame)  // ← erstellt neuen State ohne History
    notifyObservers
  }

  def save(): Unit =
    fileIO.save(state.currentGame) match {
      case Success(_) => println("Spiel gespeichert!")
      case Failure(ex) => println(s"Fehler beim Speichern: ${ex.getMessage}")
    }

  def load(): Unit =
    fileIO.load() match {
      case Success(loadedGame) =>
        setGame(loadedGame)
        println("Spiel geladen!")
      case Failure(ex) => println(s"Fehler beim Laden: ${ex.getMessage}")
    }