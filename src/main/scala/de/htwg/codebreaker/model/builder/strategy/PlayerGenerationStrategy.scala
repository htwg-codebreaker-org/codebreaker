package de.htwg.codebreaker.model.builder.strategy

import de.htwg.codebreaker.model.map.{WorldMap, Tile}
import de.htwg.codebreaker.model.player.Player



trait PlayerGenerationStrategy {
  def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player]
}
