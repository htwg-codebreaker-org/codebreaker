package de.htwg.codebreaker.model.game.strategy

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game.strategy.ServerGenerator

object DefaultServerStrategy extends ServerGenerationStrategy {
  override def generateServers(map: WorldMap): List[Server] = {
    val continents   = Continent.values.filter(_.isLand).toList
    val fixedServers = ServerGenerator.generateFixedServers(map)
    val sideServers  =
      continents.flatMap(c => ServerGenerator.generateSideServersFor(c, map, fixedServers))
    fixedServers ++ sideServers
  }
}
