package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model.builder.GameFactory

class GameFactorySpec extends AnyWordSpec with Matchers:

  "GameFactory" should {

    "create easy game" in {
      val game = GameFactory("easy")
      
      game should not be null
      game.model.players should not be empty
      game.state.round shouldBe 1
    }

    "create unlockAll game" in {
      val game = GameFactory("unlockAll")

      game should not be null
      game.model.players should not be empty
      game.state.round shouldBe 1
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
      game.model.map should not be null
      game.state shouldBe GameState(Some(0), GameStatus.Running, Phase.AwaitingInput, 1)
    }

    "create unlockAll game explicitly" in {
      val game = GameFactory.unlockAll()

      game should not be null
      game.model.players should have size 2
      game.model.servers should not be empty
      game.model.map should not be null
      game.state shouldBe GameState(Some(0), GameStatus.Running, Phase.AwaitingInput, 1)
    }

    "create games with proper initial state" in {
      val game = GameFactory.default()
      
      game.state.status shouldBe GameStatus.Running
      game.state.phase shouldBe Phase.AwaitingInput
      game.state.round shouldBe 1
      game.state.currentPlayerIndex shouldBe Some(0)
    }

    "create games with proper world map" in {
      val game = GameFactory.default()
      
      game.model.map.width shouldBe 80
      game.model.map.height shouldBe 40
    }

    "create games with players" in {
      val game = GameFactory.default()
      
      game.model.players should have size 2
      game.model.players.foreach { player =>
        player.id should be >= 0
        player.name should not be empty
        player.laptop.hardware.cpu should be > 0
        player.laptop.hardware.ram should be > 0
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
      
      game.model.hackSkills should not be empty
      game.model.hackSkills.foreach { skill =>
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
      val unlockAllGame = GameFactory("unlockAll")
      val customGame = GameFactory("custom")

      easyGame should not be null
      unlockAllGame should not be null
      customGame should not be null
    }
  }
