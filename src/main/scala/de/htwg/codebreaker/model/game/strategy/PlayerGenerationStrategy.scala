package de.htwg.codebreaker.model.game.strategy

import de.htwg.codebreaker.model._

trait PlayerGenerationStrategy {
  def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player]
}
