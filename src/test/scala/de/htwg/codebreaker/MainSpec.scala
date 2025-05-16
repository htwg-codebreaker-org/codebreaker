package de.htwg.codebreaker

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.codebreaker.model.game.GameFactory
import de.htwg.codebreaker.controller.Controller

class MainSpec extends AnyWordSpec with Matchers {

  "The main game entry point" should {
    "create a controller and game state without throwing" in {
      noException should be thrownBy {
        val (model, state) = GameFactory.createDefaultGame()
        val controller = Controller(model, state)
        controller.getPlayers should not be empty
      }
    }
  }
}
