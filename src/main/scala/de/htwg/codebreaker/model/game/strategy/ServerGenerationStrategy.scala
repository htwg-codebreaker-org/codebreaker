package de.htwg.codebreaker.model.game.strategy

import de.htwg.codebreaker.model._

trait ServerGenerationStrategy {
  def generateServers(map: WorldMap): List[Server]
}
