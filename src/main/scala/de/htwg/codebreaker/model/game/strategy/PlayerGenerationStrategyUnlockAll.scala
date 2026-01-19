package de.htwg.codebreaker.model.game.strategy

import de.htwg.codebreaker.model.map.{WorldMap, Tile}
import de.htwg.codebreaker.model.player.Player


trait PlayerGenerationStrategyUnlockAll {
  def generatePlayersUnlockAll(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player]
}
