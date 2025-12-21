package de.htwg.codebreaker.controller

import com.google.inject.Inject
import de.htwg.codebreaker.util.Observable
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.persistence.FileIOInterface
import scala.util.{Try, Success, Failure}

/**
 * Implementation of the game controller component.
 * Manages game state, command execution, undo/redo functionality, and observer notifications.
 * This class encapsulates all game logic and state management, exposing only the interface methods.
 *
 * @param initialGame The initial game state, injected by Guice
 * @param fileIO The file I/O implementation, injected by Guice
 */
class Controller @Inject() (initialGame: Game, fileIO: FileIOInterface) extends ControllerInterface with Observable:

  private var currentGame: Game = initialGame

  def game: Game = currentGame

  def canUndo: Boolean = undoStack.nonEmpty
  def canRedo: Boolean = redoStack.nonEmpty


  def getPlayers: List[Player] = game.model.players
  def getServers: List[Server] = game.model.servers
  def getMapData(): Vector[Vector[MapObject]] =
    game.model.worldMap.getMapData(game.model.players, game.model.servers)
  def getState: GameState = game.state

  private case class HistoryEntry(gameBefore: Game, command: Command)

  private var undoStack: List[HistoryEntry] = Nil
  private var redoStack: List[HistoryEntry] = Nil


  /** Führt ein Command aus und speichert den alten Zustand für Undo. */
  def doAndRemember(cmd: Command): Unit = {
    import scala.util.{Success, Failure}
    cmd.doStep(game) match {
      case Success(newGame) =>
        undoStack = HistoryEntry(game, cmd) :: undoStack
        redoStack = Nil // Redo wird ungültig bei neuem Schritt
        currentGame = newGame
        save() // Auto-save after each command
        notifyObservers
      case Failure(ex) =>
        println(s"Command fehlgeschlagen: ${ex.getMessage}")
    }
  }


  /** Macht den letzten Schritt rückgängig. */
  def undo(): Unit = undoStack match {
    case HistoryEntry(before, cmd) :: rest =>
      cmd.undoStep(game) match {
        case Success(revertedGame) =>
          redoStack = HistoryEntry(game, cmd) :: redoStack // aktueller Zustand in redo
          currentGame = revertedGame
          undoStack = rest
          notifyObservers
        case Failure(ex) =>
          println(s"Undo fehlgeschlagen: ${ex.getMessage}")
      }
    case Nil => println("Nichts zum Rückgängig machen.")
  }


  /** Stellt einen rückgängig gemachten Schritt wieder her. */
  def redo(): Unit = redoStack match {
    case HistoryEntry(before, cmd) :: rest =>
      cmd.doStep(game) match {
        case Success(redoneGame) =>
          undoStack = HistoryEntry(game, cmd) :: undoStack // aktueller Zustand in undo
          currentGame = redoneGame
          redoStack = rest
          notifyObservers
        case Failure(ex) =>
          println(s"Redo fehlgeschlagen: ${ex.getMessage}")
      }
    case Nil => println("Nichts zum Wiederholen.")
  }


  // Die direkte unclaimServer-Methode wird entfernt, da alles über das Command-Pattern laufen soll

  def advanceRound(): Unit =
    currentGame = game.copy(state = game.state.advanceRound())
    notifyObservers


  def setPhase(newPhase: Phase): Unit =
    currentGame = game.copy(state = game.state.setPhase(newPhase))
    notifyObservers

  def setStatus(newStatus: GameStatus): Unit =
    currentGame = game.copy(state = game.state.setStatus(newStatus))
    notifyObservers

  def setGame(newGame: Game): Unit =
    currentGame = newGame
    undoStack = Nil
    redoStack = Nil
    notifyObservers

  def save(): Unit =
    fileIO.save(game) match {
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
