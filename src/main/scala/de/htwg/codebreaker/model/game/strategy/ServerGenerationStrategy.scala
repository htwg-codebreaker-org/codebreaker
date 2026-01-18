package de.htwg.codebreaker.model.game.strategy


import de.htwg.codebreaker.model.map.WorldMap
import de.htwg.codebreaker.model.server.Server

trait ServerGenerationStrategy {
  def generateServers(map: WorldMap): List[Server]
}
