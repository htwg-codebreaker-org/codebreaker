package de.htwg.codebreaker.controller

import de.htwg.codebreaker.util.Observable
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._

class Controller(initialGame: Game) extends Observable:

  private var currentGame: Game = initialGame

  def game: Game = currentGame

  def getPlayers: List[Player] = game.model.players
  def getServers: List[Server] = game.model.servers
  def getMapData(): Vector[Vector[MapObject]] =
    game.model.worldMap.getMapData(game.model.players, game.model.servers)
  def getState: GameState = game.state

  def claimServer(serverName: String, playerIndex: Int): Unit =
    val updatedServers = game.model.servers.map {
      case s if s.name == serverName => Server.claim(s, playerIndex)
      case s => s
    }
    val updatedModel = game.model.copy(servers = updatedServers)
    currentGame = game.copy(model = updatedModel)
    notifyObservers

  def unclaimServer(serverName: String): Unit =
    val updatedServers = game.model.servers.map {
      case s if s.name == serverName => Server.unclaim(s)
      case s => s
    }
    val updatedModel = game.model.copy(servers = updatedServers)
    currentGame = game.copy(model = updatedModel)
    notifyObservers

  def advanceRound(): Unit =
    currentGame = game.copy(state = game.state.advanceRound())
    notifyObservers

  def nextPlayer(totalPlayers: Int): Unit =
    currentGame = game.copy(state = game.state.nextPlayer(totalPlayers))
    notifyObservers

  def setPhase(newPhase: Phase): Unit =
    currentGame = game.copy(state = game.state.setPhase(newPhase))
    notifyObservers

  def setStatus(newStatus: GameStatus): Unit =
    currentGame = game.copy(state = game.state.setStatus(newStatus))
    notifyObservers
