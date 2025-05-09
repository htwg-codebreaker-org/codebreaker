package de.htwg.codebreaker.model.game
import de.htwg.codebreaker.model._

// model/game/GameModel.scala
case class GameModel(
  var players: List[Player] = List(),
  var servers: List[Server] = List(),
  var worldMap: WorldMap = WorldMap.defaultMap
)
