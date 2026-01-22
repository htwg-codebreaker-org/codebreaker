package de.htwg.codebreaker.controller.commands

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.model.builder.{GameBuilder, GameFactory}
import de.htwg.codebreaker.model.map.WorldMap


trait CommandTestBase extends AnyWordSpec with Matchers {

  def baseGame: Game =
    GameFactory.default()
}
