package de.htwg.codebreaker.model.game.strategy

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game.strategy.PlayerGenerator

object RandomPlayerGenerator extends PlayerGenerationStrategy {
  override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] =
    PlayerGenerator.generatePlayers(numPlayers, map, avoidTiles)
}
