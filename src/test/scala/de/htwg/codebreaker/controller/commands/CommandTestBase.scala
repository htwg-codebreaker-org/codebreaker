package de.htwg.codebreaker.controller.commands

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.model.game.builder.GameBuilder
import de.htwg.codebreaker.model.game.strategy._


trait CommandTestBase extends AnyWordSpec with Matchers {

  def baseGame: Game =
    GameBuilder()
      .withNumberOfPlayers(2)
      .withServerStrategy(DefaultServerStrategy)
      .withSkills(DefaultSkillTreeGenerator)
      .withMap(WorldMap.defaultMap)
      .build()
}
