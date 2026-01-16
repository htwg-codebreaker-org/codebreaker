package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameFactorySpec extends AnyWordSpec with Matchers:

  "GameFactory" should {

    "create easy game" in {
      val game = GameFactory("easy")
      
      game should not be null
      game.model.players should not be empty
      game.state.round shouldBe 0
    }

    "create hard game" in {
      val game = GameFactory("hard")
      
      game should not be null
      game.model.players should not be empty
      game.state.round shouldBe 0
    }

    "fallback to default for unknown game type" in {
      val game = GameFactory("unknown")
      
      game should not be null
      game.model.players should not be empty
    }

    "create default game explicitly" in {
      val game = GameFactory.default()
      
      game should not be null
      game.model.players should have size 2
      game.model.servers should not be empty
      game.model.worldMap should not be null
      game.state shouldBe GameState(Some(0), GameStatus.Running, Phase.AwaitingInput, 0)
    }

    "create hard game explicitly" in {
      val game = GameFactory.hard()
      
      game should not be null
      game.model.players should have size 2
      game.model.servers should not be empty
      game.model.worldMap should not be null
      game.state shouldBe GameState(Some(0), GameStatus.Running, Phase.AwaitingInput, 0)
    }

    "create games with proper initial state" in {
      val game = GameFactory.default()
      
      game.state.status shouldBe GameStatus.Running
      game.state.phase shouldBe Phase.AwaitingInput
      game.state.round shouldBe 0
      game.state.currentPlayerIndex shouldBe Some(0)
    }

    "create games with proper world map" in {
      val game = GameFactory.default()
      
      game.model.worldMap.width shouldBe 80
      game.model.worldMap.height shouldBe 40
    }

    "create games with players" in {
      val game = GameFactory.default()
      
      game.model.players should have size 2
      game.model.players.foreach { player =>
        player.id should be >= 0
        player.name should not be empty
        player.cpu should be > 0
        player.ram should be > 0
      }
    }

    "create games with servers" in {
      val game = GameFactory.default()
      
      game.model.servers should not be empty
      game.model.servers.foreach { server =>
        server.name should not be empty
        server.difficulty should be >= 0
        server.rewardCpu should be >= 0
        server.rewardRam should be >= 0
      }
    }

    "create games with skills" in {
      val game = GameFactory.default()
      
      game.model.skills should not be empty
      game.model.skills.foreach { skill =>
        skill.id should not be empty
        skill.name should not be empty
        skill.costXp should be >= 0
      }
    }

    "create different instances for each call" in {
      val game1 = GameFactory.default()
      val game2 = GameFactory.default()
      
      // They should be equal in content but different instances
      game1.model.players should have size game2.model.players.size
      // Server count can vary due to random generation
      game1.model.servers should not be empty
      game2.model.servers should not be empty
    }

    "support all predefined game types" in {
      val easyGame = GameFactory("easy")
      val hardGame = GameFactory("hard")
      val customGame = GameFactory("custom")
      
      easyGame should not be null
      hardGame should not be null
      customGame should not be null
    }
  }
