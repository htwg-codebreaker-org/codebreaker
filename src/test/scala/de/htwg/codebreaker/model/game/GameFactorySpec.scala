package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameFactorySpec extends AnyWordSpec with Matchers {

  "GameFactory" should {

    "return a default game when called with 'easy'" in {
      val game = GameFactory("easy")
      game should not be null
      game.model.players.size shouldBe 2

    }

    "return a hard game when called with 'hard'" in {
      val game = GameFactory("hard")
      game should not be null
      game.model.players.size shouldBe 2

    }

    "fallback to default game when called with unknown kind" in {
      val game = GameFactory("custom")
      game should not be null
      game.model.players.size shouldBe 2

    }

    "create default game explicitly" in {
      val game = GameFactory.default()
      game should not be null
    }

    "create hard game explicitly" in {
      val game = GameFactory.hard()
      game should not be null
    }
  }
}
