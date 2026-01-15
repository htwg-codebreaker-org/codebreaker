package de.htwg.codebreaker.module

import de.htwg.codebreaker.model.game.Game
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameProviderSpec extends AnyWordSpec with Matchers {

  "GameProvider" should {

    "provide a Game instance" in {
      val provider = new GameProvider()
      val game     = provider.get()

      game shouldBe a[Game]
    }

    "provide a default game with players and servers" in {
      val provider = new GameProvider()
      val game     = provider.get()

      // Default game should have players
      game.model.players should not be empty

      // Default game should have servers
      game.model.servers should not be empty

      // Default game should have a world map
      game.model.worldMap should not be null
    }

    "provide a new game instance on each call" in {
      val provider = new GameProvider()
      val game1    = provider.get()
      val game2    = provider.get()

      // They should be different instances
      game1 should not be theSameInstanceAs(game2)

      // Both should be valid games with players and servers
      game1.model.players should not be empty
      game2.model.players should not be empty
      game1.model.servers should not be empty
      game2.model.servers should not be empty
    }

    "provide a game in Running state" in {
      val provider = new GameProvider()
      val game     = provider.get()

      game.state.status.toString shouldBe "Running"
    }
  }
}
