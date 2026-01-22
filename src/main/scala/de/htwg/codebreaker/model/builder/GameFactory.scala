// src/main/scala/de/htwg/codebreaker/model/game/GameFactory.scala
package de.htwg.codebreaker.model.builder

import de.htwg.codebreaker.model.map.WorldMap
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.builder.strategy.generator.player.UnlockAllPlayerGenerator
import de.htwg.codebreaker.model.builder.strategy.generator.server.DefaultServerGenerator
import de.htwg.codebreaker.model.builder.strategy.generator.skilltree.DefaultSkillTreeGenerator
import de.htwg.codebreaker.model.builder.strategy.generator.server.DefaultServerRoleGenerator
import de.htwg.codebreaker.model.builder.strategy.generator.player.laptop.DefaultLaptopToolGenerator


object GameFactory {

  def apply(kind: String): Game = kind match {
    case "easy" => default()
    case "unlockAll" => unlockAll()
    case _      => default() // fallback
  }

  def default(): Game =
    GameBuilder()
      .withNumberOfPlayers(2)
      .withServerStrategy(DefaultServerGenerator)
      .withMap(WorldMap.defaultMap)
      .withSkills(DefaultSkillTreeGenerator)
      .withRoleStrategy(DefaultServerRoleGenerator)
      .withLaptopTools(DefaultLaptopToolGenerator)
      .build()

  def unlockAll(): Game =
    GameBuilder()
      .withNumberOfPlayers(2)
      .withPlayerStrategy(UnlockAllPlayerGenerator)
      .withServerStrategy(DefaultServerGenerator)
      .withMap(WorldMap.defaultMap)
      .withSkills(DefaultSkillTreeGenerator)
      .withRoleStrategy(DefaultServerRoleGenerator)
      .withLaptopTools(DefaultLaptopToolGenerator)
      .build()
}

