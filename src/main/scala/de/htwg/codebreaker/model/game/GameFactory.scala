package de.htwg.codebreaker.model.game

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game.strategy._

object GameFactory {

  def createGameWithStrategies(
      playerStrategy: PlayerGenerationStrategy,
      serverStrategy: ServerGenerationStrategy
  ): (GameModel, GameState) = {

    val map = WorldMap.defaultMap
    val servers = serverStrategy.generateServers(map)
    val takenTiles = servers.map(_.tile)
    val players = playerStrategy.generatePlayers(2, map, avoidTiles = takenTiles)

    val model = GameModel(players, servers, map)
    val state = GameState()

    (model, state)
  }

  // Standard-Spiel mit Defaults
  def createDefaultGame(): (GameModel, GameState) = {
    createGameWithStrategies(RandomPlayerGenerator, DefaultServerStrategy)
  }
}