// src/main/scala/de/htwg/codebreaker/model/game/builder/GameBuilder.scala
package de.htwg.codebreaker.model.game.builder

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.model.game.strategy._

final class GameBuilder private (
    val numPlayers: Int = 2,
    val playerStrategy: PlayerGenerationStrategy = RandomPlayerGenerator,
    val serverStrategy: ServerGenerationStrategy = DefaultServerStrategy,
    val map: WorldMap = WorldMap.defaultMap
):

  def withNumberOfPlayers(n: Int): GameBuilder =
    GameBuilder(n, playerStrategy, serverStrategy, map)

  def withPlayerStrategy(ps: PlayerGenerationStrategy): GameBuilder =
    GameBuilder(numPlayers, ps, serverStrategy, map)

  def withServerStrategy(ss: ServerGenerationStrategy): GameBuilder =
    GameBuilder(numPlayers, playerStrategy, ss, map)

  def withMap(newMap: WorldMap): GameBuilder =
    GameBuilder(numPlayers, playerStrategy, serverStrategy, newMap)

  def build(): Game =
    val servers = serverStrategy.generateServers(map)
    val takenTiles = servers.map(_.tile)
    val players = playerStrategy.generatePlayers(numPlayers, map, takenTiles)
    Game(GameModel(players, servers, map), GameState())


object GameBuilder:
  def apply(): GameBuilder =
    new GameBuilder()

  def apply(
      numPlayers: Int,
      playerStrategy: PlayerGenerationStrategy,
      serverStrategy: ServerGenerationStrategy,
      map: WorldMap
  ): GameBuilder =
    new GameBuilder(numPlayers, playerStrategy, serverStrategy, map)
