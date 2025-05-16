package de.htwg.codebreaker.controller
import de.htwg.codebreaker.util.Observable
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._


class Controller(
    var model: GameModel,
    var state: GameState
) extends Observable:

  def claimServer(serverName: String, playerIndex: Int): Unit =
    model.servers.find(_.name == serverName).foreach { server =>
      val updated = Server.claim(server, playerIndex)
      model.servers = model.servers.map(s => if s.name == serverName then updated else s)
      notifyObservers
    }

  def unclaimServer(serverName: String): Unit =
    model.servers.find(_.name == serverName).foreach { server =>
      val updated = Server.unclaim(server)
      model.servers = model.servers.map(s => if s.name == serverName then updated else s)
      notifyObservers
    }

  def getPlayers: List[Player] = model.players

  def getServers: List[Server] = model.servers

  def getMapData(): Vector[Vector[MapObject]] =
    model.worldMap.getMapData(model.players, model.servers)