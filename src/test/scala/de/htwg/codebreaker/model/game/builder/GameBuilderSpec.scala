package de.htwg.codebreaker.model.game.builder

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.model.game.strategy._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameBuilderSpec extends AnyWordSpec with Matchers {

  "A GameBuilder" should {

    "create a default game with default settings" in {
      val game = GameBuilder().build()

      game shouldBe a[Game]
      game.model.players should have length 2 // Default: 2 players
      game.model.servers should not be empty
      game.model.worldMap shouldBe WorldMap.defaultMap

      // Game state checks
      game.state.currentPlayerIndex shouldBe Some(0) // Player 0 starts
      game.state.status shouldBe GameStatus.Running
      game.state.phase shouldBe Phase.AwaitingInput
      game.state.round shouldBe 1
    }

    "allow customization of number of players" in {
      val game = GameBuilder()
        .withNumberOfPlayers(4)
        .build()

      game.model.players should have length 4
    }

    "allow customization of player strategy" in {
      // Create a custom player strategy that always places players at (0,0)
      val customStrategy = new PlayerGenerationStrategy {
        override def generatePlayers(
          count: Int,
          map: WorldMap,
          avoidTiles: List[Tile]
        ): List[Player] = {
          val tile = Tile(0, 0, Continent.Europe)
          (0 until count).map { i =>
            Player(
              id = i,
              name = s"Player$i",
              tile = tile,
              cpu = 100,
              ram = 100,
              code = 0,
              level = 1,
              xp = 0,
              cybersecurity = 0,
              movementPoints = 5
            )
          }.toList
        }
      }

      val game = GameBuilder()
        .withPlayerStrategy(customStrategy)
        .build()

      game.model.players.foreach { player =>
        player.tile.x shouldBe 0
        player.tile.y shouldBe 0
      }
    }

    "allow customization of server strategy" in {
      // Create a custom server strategy that generates exactly 3 servers
      val customStrategy = new ServerGenerationStrategy {
        override def generateServers(map: WorldMap): List[Server] =
          List(
            Server("S1", Tile(1, 1, Continent.Europe), 10, 5, 3, false, ServerType.Firm),
            Server("S2", Tile(2, 2, Continent.Europe), 20, 10, 6, false, ServerType.Cloud),
            Server("S3", Tile(3, 3, Continent.Europe), 30, 15, 9, false, ServerType.Bank)
          )
      }

      val game = GameBuilder()
        .withServerStrategy(customStrategy)
        .build()

      game.model.servers should have length 3
      game.model.servers.map(_.name) should contain allOf ("S1", "S2", "S3")
    }

    "allow customization of world map" in {
      val defaultMap = WorldMap.defaultMap
      val game       = GameBuilder()
        .withMap(defaultMap)
        .build()

      game.model.worldMap shouldBe defaultMap
    }

    "support method chaining for fluent API" in {
      val game = GameBuilder()
        .withNumberOfPlayers(3)
        .withMap(WorldMap.defaultMap)
        .build()

      game.model.players should have length 3
      game.model.worldMap shouldBe WorldMap.defaultMap
    }

    "create immutable builders (each with* returns new instance)" in {
      val builder1 = GameBuilder()
      val builder2 = builder1.withNumberOfPlayers(5)

      // Original builder should be unchanged
      builder1.numPlayers shouldBe 2
      builder2.numPlayers shouldBe 5

      // Should be different instances
      builder1 should not be theSameInstanceAs(builder2)
    }

    "ensure players don't spawn on server tiles" in {
      val game = GameBuilder()
        .withNumberOfPlayers(10) // More players to test collision avoidance
        .build()

      val playerTiles = game.model.players.map(_.tile).toSet
      val serverTiles = game.model.servers.map(_.tile).toSet

      // No overlap between player and server positions
      playerTiles.intersect(serverTiles) shouldBe empty
    }

    "initialize all players with correct default stats" in {
      val game = GameBuilder()
        .withNumberOfPlayers(3)
        .build()

      game.model.players.zipWithIndex.foreach {
        case (player, index) =>
          player.id shouldBe index + 1 // IDs start at 1
          player.cpu shouldBe 100
          player.ram shouldBe 50
          player.code shouldBe 10
          player.level shouldBe 1
          player.xp shouldBe 0
          player.cybersecurity shouldBe 20
          player.movementPoints shouldBe 5
          player.maxMovementPoints shouldBe 5
      }
    }

    "place all players on land tiles" in {
      val game = GameBuilder()
        .withNumberOfPlayers(5)
        .build()

      game.model.players.foreach { player =>
        player.tile.continent.isLand shouldBe true
      }
    }

    "generate servers with valid properties" in {
      val game = GameBuilder().build()

      game.model.servers.foreach { server =>
        server.name should not be empty
        server.difficulty should be >= 0
        server.difficulty should be <= 100
        server.rewardCpu should be >= 0
        server.rewardRam should be >= 0
        server.hacked shouldBe false
        server.hackedBy shouldBe None
        server.claimedBy shouldBe None
      }
    }

    "support applying custom parameters directly" in {
      val customStrategy = new PlayerGenerationStrategy {
        override def generatePlayers(
          count: Int,
          map: WorldMap,
          avoidTiles: List[Tile]
        ): List[Player] =
          List(Player(0, "TestPlayer", Tile(5, 5, Continent.Europe), 100, 100, 0, 1, 0, 0, 5))
      }

      val builder = GameBuilder(
        numPlayers = 1,
        playerStrategy = customStrategy,
        serverStrategy = DefaultServerStrategy,
        map = WorldMap.defaultMap
      )

      val game = builder.build()
      game.model.players should have length 1
      game.model.players.head.name shouldBe "TestPlayer"
    }

    "create consistent game state across multiple builds" in {
      val builder = GameBuilder()
        .withNumberOfPlayers(2)

      val game1 = builder.build()
      val game2 = builder.build()

      // Both games should have same configuration
      game1.model.players.length shouldBe game2.model.players.length
      game1.state.currentPlayerIndex shouldBe game2.state.currentPlayerIndex
      game1.state.status shouldBe game2.state.status
      game1.state.phase shouldBe game2.state.phase
      game1.state.round shouldBe game2.state.round
    }

    "allow building multiple different games from same builder" in {
      val builder = GameBuilder()

      val game1 = builder.withNumberOfPlayers(2).build()
      val game2 = builder.withNumberOfPlayers(4).build()

      game1.model.players should have length 2
      game2.model.players should have length 4
    }
  }
}
