package de.htwg.codebreaker.controller
import de.htwg.codebreaker.util.Observable
import de.htwg.codebreaker.model._


class Controller extends Observable:
  private var servers: List[Server] = List()  //parameter von controller
  private var worldMap: WorldMap = WorldMap.defaultMap
  private var players: List[Player] = List()


  def setServers(newServers: List[Server]): Unit =  // weg lassen für di
    servers = newServers
  
  def claimServer(serverName: String, playerIndex: Int): Unit =
    servers.find(_.name == serverName).foreach { server =>
      val updated = Server.claim(server, playerIndex)
      servers = servers.map(s => if s.name == serverName then updated else s)
      notifyObservers
    }

  def unclaimServer(serverName: String): Unit =
    servers.find(_.name == serverName).foreach { server =>
      val updated = Server.unclaim(server)
      servers = servers.map(s => if s.name == serverName then updated else s)
      notifyObservers
    }

  def getServers: List[Server] = servers

  def createPlayer(name: String): Player = {
    val player = Player(1, name, Tile(1,1,Continent.Africa), 50, 20, 10, 1, 0, 20)
    notifyObservers
    player
  }
  
  def getMapData(): Vector[Vector[MapObject]] =
    worldMap.getMapData(players, servers)