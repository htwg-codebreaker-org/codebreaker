// src/main/scala/de/htwg/codebreaker/model/game/GameFactory.scala
package de.htwg.codebreaker.model.game

import de.htwg.codebreaker.model.game.builder.GameBuilder
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game.strategy._

object GameFactory {

  def apply(kind: String): Game = kind match {
    case "easy" => default()
    case "hard" => hard()
    case _      => default() // fallback
  }

  def default(): Game =
    GameBuilder()
      .withNumberOfPlayers(2)
      .withServerStrategy(DefaultServerStrategy)
      .withMap(WorldMap.defaultMap)
      .withSkills(DefaultSkillTreeGenerator)
      .build()

  def hard(): Game =
    GameBuilder()
      .withNumberOfPlayers(2)
      .withServerStrategy(DefaultServerStrategy)
      .withMap(WorldMap.defaultMap)
      .withSkills(DefaultSkillTreeGenerator)
      .build()
}

