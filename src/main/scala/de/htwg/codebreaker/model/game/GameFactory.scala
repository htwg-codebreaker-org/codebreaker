// src/main/scala/de/htwg/codebreaker/model/game/GameFactory.scala
package de.htwg.codebreaker.model.game

import de.htwg.codebreaker.model.map.WorldMap
import de.htwg.codebreaker.model.game.game.Game
import de.htwg.codebreaker.model.game.strategy.server.DefaultServerGenerator
import de.htwg.codebreaker.model.game.strategy.skilltree.DefaultSkillTreeGenerator
import de.htwg.codebreaker.model.game.builder.GameBuilder
import de.htwg.codebreaker.model.game.strategy.server.DefaultServerRoleGenerator
import de.htwg.codebreaker.model.game.strategy.laptop.DefaultLaptopToolGenerator


object GameFactory {

  def apply(kind: String): Game = kind match {
    case "easy" => default()
    case "hard" => hard()
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

  def hard(): Game =
    GameBuilder()
      .withNumberOfPlayers(2)
      .withServerStrategy(DefaultServerGenerator)
      .withMap(WorldMap.defaultMap)
      .withSkills(DefaultSkillTreeGenerator)
      .withRoleStrategy(DefaultServerRoleGenerator)
      .withLaptopTools(DefaultLaptopToolGenerator)
      .build()
}

