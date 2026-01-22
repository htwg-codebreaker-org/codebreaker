// src/main/scala/de/htwg/codebreaker/model/game/builder/GameBuilder.scala
package de.htwg.codebreaker.model.game.builder

import de.htwg.codebreaker.model.map.WorldMap
import de.htwg.codebreaker.model.game.game.{Game, GameModel, GameState, GameStatus, Phase}
import de.htwg.codebreaker.model.game.strategy.server.{DefaultServerGenerator}
import de.htwg.codebreaker.model.game.strategy.skilltree.{DefaultSkillTreeGenerator}
import de.htwg.codebreaker.model.game.strategy.{PlayerGenerationStrategy, ServerGenerationStrategy, SkillTreeGenerationStrategy}
import de.htwg.codebreaker.model.game.strategy.server.{DefaultServerRoleGenerator}
import de.htwg.codebreaker.model.game.strategy.ServerRoleGenerationStrategy
import de.htwg.codebreaker.model.player.laptop.LaptopTool
import de.htwg.codebreaker.model.game.strategy.player.{DefaultPlayerGenerator, UnlockAllPlayerGenerator}
import de.htwg.codebreaker.model.game.strategy.LaptopToolGenerationStrategy
import de.htwg.codebreaker.model.game.strategy.laptop.DefaultLaptopToolGenerator



final class GameBuilder private (
    val numPlayers: Int = 2,
    val playerStrategy: PlayerGenerationStrategy = DefaultPlayerGenerator,
    val serverStrategy: ServerGenerationStrategy = DefaultServerGenerator,
    val skillStrategy: SkillTreeGenerationStrategy = DefaultSkillTreeGenerator,
    val roleStrategy: ServerRoleGenerationStrategy = DefaultServerRoleGenerator,
    val map: WorldMap = WorldMap.defaultMap,
    val laptopTools: LaptopToolGenerationStrategy = DefaultLaptopToolGenerator,
):

  def withNumberOfPlayers(n: Int): GameBuilder =
    GameBuilder(n, playerStrategy, serverStrategy, skillStrategy, roleStrategy, map, laptopTools)

  def withPlayerStrategy(ps: PlayerGenerationStrategy): GameBuilder =
    GameBuilder(numPlayers, ps, serverStrategy, skillStrategy, roleStrategy, map, laptopTools)

  def withServerStrategy(ss: ServerGenerationStrategy): GameBuilder =
    GameBuilder(numPlayers, playerStrategy, ss, skillStrategy, roleStrategy, map, laptopTools)

  def withSkills(sts: SkillTreeGenerationStrategy): GameBuilder =
    GameBuilder(numPlayers, playerStrategy, serverStrategy, sts, roleStrategy, map, laptopTools)

  def withMap(newMap: WorldMap): GameBuilder =
    GameBuilder(numPlayers, playerStrategy, serverStrategy, skillStrategy, roleStrategy, newMap, laptopTools)

  def withRoleStrategy(rts: ServerRoleGenerationStrategy): GameBuilder =
    GameBuilder(numPlayers, playerStrategy, serverStrategy, skillStrategy, rts, map, laptopTools)

  def withLaptopTools(lts: LaptopToolGenerationStrategy): GameBuilder =
    GameBuilder(numPlayers, playerStrategy, serverStrategy, skillStrategy, roleStrategy, map, lts)

  def build(): Game =
    val servers = serverStrategy.generateServers(map)
    val takenTiles = servers.map(_.tile)
    val players = playerStrategy.generatePlayers(numPlayers, map, takenTiles)
    
    val (hackSkills, socialSkills) = skillStrategy.generateSkills()
    val roleBlueprints = roleStrategy.generateRoles()
    val actionBlueprints = roleStrategy.generateActions()
    val generatedLaptopTools = laptopTools.generateLaptopTools()

    val model = GameModel(players, servers, map, hackSkills, socialSkills, roleBlueprints, actionBlueprints, generatedLaptopTools)

    val state = GameState(
      currentPlayerIndex = Some(0),
      status = GameStatus.Running,
      phase = Phase.AwaitingInput,
      round = 1
    )

    Game(model, state)

object GameBuilder:
  def apply(): GameBuilder =
    new GameBuilder()

  def apply(
      numPlayers: Int,
      playerStrategy: PlayerGenerationStrategy,
      serverStrategy: ServerGenerationStrategy,
      skillStrategy: SkillTreeGenerationStrategy,
      roleStrategy: ServerRoleGenerationStrategy,
      map: WorldMap,
      laptopTools: LaptopToolGenerationStrategy
  ): GameBuilder =
    new GameBuilder(numPlayers, playerStrategy, serverStrategy, skillStrategy, roleStrategy, map, laptopTools)