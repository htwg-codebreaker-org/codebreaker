package de.htwg.codebreaker.model.game.strategy.server

import de.htwg.codebreaker.model.map.{WorldMap, Continent}
import de.htwg.codebreaker.model.server.Server
import de.htwg.codebreaker.model.game.strategy.server.ServerGenerator
import de.htwg.codebreaker.model.game.strategy.ServerGenerationStrategy

object DefaultServerGenerator extends ServerGenerationStrategy {
  override def generateServers(map: WorldMap): List[Server] = {
    val continents = Continent.values.filter(_.isLand).toList
    val fixedServers = ServerGenerator.generateFixedServers(map)
    val sideServers = continents.flatMap(c => ServerGenerator.generateSideServersFor(c, map, fixedServers))
    fixedServers ++ sideServers
  }
}
