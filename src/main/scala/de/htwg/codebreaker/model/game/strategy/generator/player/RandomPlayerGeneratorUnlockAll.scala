package de.htwg.codebreaker.model.game.strategy.player

import de.htwg.codebreaker.model.map.{WorldMap, Tile}
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.game.strategy.PlayerGenerationStrategy

object RandomPlayerGeneratorUnlockAll extends PlayerGenerationStrategy {
  override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] =
    PlayerGenerator.generatePlayers(numPlayers, map, avoidTiles)
}
