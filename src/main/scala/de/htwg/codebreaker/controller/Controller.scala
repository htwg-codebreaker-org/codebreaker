package de.htwg.codebreaker.controller

import de.htwg.codebreaker.util.Observable
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._

class Controller(initialGame: Game) extends Observable:

  private var currentGame: Game = initialGame

  def game: Game = currentGame

  def canUndo: Boolean = undoStack.nonEmpty
  def canRedo: Boolean = redoStack.nonEmpty


  def getPlayers: List[Player] = game.model.players
  def getServers: List[Server] = game.model.servers
  def getMapData(): Vector[Vector[MapObject]] =
    game.model.worldMap.getMapData(game.model.players, game.model.servers)
  def getState: GameState = game.state

  // Undo/Redo Stack (funktional, immutable)
  private var undoStack: List[Game] = Nil
  private var redoStack: List[Game] = Nil

  /** Führt ein Command aus und speichert den alten Zustand für Undo. */
  def doAndRemember(cmd: Command): Unit = {
    import scala.util.{Success, Failure}
    cmd.doStep(game) match {
      case Success(newGame) =>
        undoStack = game :: undoStack
        currentGame = newGame
        redoStack = Nil
        notifyObservers
      case Failure(ex) =>
        println(s"Command fehlgeschlagen: ${ex.getMessage}")
    }
  }

  /** Macht den letzten Schritt rückgängig. */
  def undo(): Unit = undoStack match {
    case last :: rest =>
      redoStack = game :: redoStack
      currentGame = last
      undoStack = rest
      notifyObservers
    case Nil => println("Nichts zum Rückgängig machen.")
  }

  /** Stellt einen rückgängig gemachten Schritt wieder her. */
  def redo(): Unit = redoStack match {
    case last :: rest =>
      undoStack = game :: undoStack
      currentGame = last
      redoStack = rest
      notifyObservers
    case Nil => println("Nichts zum Wiederholen.")
  }

  // Die direkte unclaimServer-Methode wird entfernt, da alles über das Command-Pattern laufen soll

  def advanceRound(): Unit =
    currentGame = game.copy(state = game.state.advanceRound())
    notifyObservers

  def nextPlayer(totalPlayers: Int): Unit =
    currentGame = game.copy(state = game.state.nextPlayer(totalPlayers))
    notifyObservers

  def nextPlayerAndNotify(): Unit =
    val totalPlayers = game.model.players.length
    val nextIndex = game.state.currentPlayerIndex match
      case Some(i) => (i + 1) % totalPlayers
      case None    => 0 // sollte nie passieren, aber besser safe than sorry

    val roundInc = if game.state.currentPlayerIndex.contains(totalPlayers - 1) then 1 else 0
    val newRound = game.state.round + roundInc

    currentGame = game.copy(state = game.state.copy(currentPlayerIndex = Some(nextIndex), round = newRound))
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
