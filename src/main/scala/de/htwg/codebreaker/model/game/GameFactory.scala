// src/main/scala/de/htwg/codebreaker/model/game/GameFactory.scala
package de.htwg.codebreaker.model.game

import de.htwg.codebreaker.model.map.WorldMap
import de.htwg.codebreaker.model.game.game.Game
import de.htwg.codebreaker.model.game.strategy.skilltree.DefaultSkillTreeGenerator
import de.htwg.codebreaker.model.game.builder.GameBuilder
import de.htwg.codebreaker.model.game.strategy.laptop.DefaultLaptopToolGenerator
import de.htwg.codebreaker.model.game.strategy.PlayerGenerationStrategy
import de.htwg.codebreaker.model.game.strategy.player.{DefaultPlayerGenerator, UnlockAllPlayerGenerator}
import de.htwg.codebreaker.model.game.strategy.ServerGenerationStrategy
import de.htwg.codebreaker.model.game.strategy.server.{DefaultServerRoleGenerator, DefaultServerGenerator}


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

