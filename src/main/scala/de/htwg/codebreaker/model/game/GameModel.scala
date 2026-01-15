package de.htwg.codebreaker.model.game
import de.htwg.codebreaker.model._

// model/game/GameModel.scala
case class GameModel(
  players: List[Player],
  servers: List[Server],
  worldMap: WorldMap
)
