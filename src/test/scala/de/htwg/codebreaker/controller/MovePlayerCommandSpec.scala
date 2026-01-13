package de.htwg.codebreaker.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game.{Game, GameModel, GameState}
import scala.util.Random

class MovePlayerCommandSpec extends AnyWordSpec with Matchers {

  "A MovePlayerCommand" should {

    "move a player to a new tile and reduce movement points" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val tile2 = Tile(2, 1, Continent.Europe) // Distance: 3
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val model = GameModel(
        players = List(player),
        servers = List(),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      val command = MovePlayerCommand(0, tile2)
      val result = command.doStep(game)

      result.isSuccess shouldBe true
      val newGame = result.get
      val movedPlayer = newGame.model.players(0)

      movedPlayer.tile shouldBe tile2
      movedPlayer.movementPoints shouldBe 2 // 5 - 3 = 2
    }

    "undo a player move correctly" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val tile2 = Tile(1, 1, Continent.Europe) // Distance: 2
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val model = GameModel(
        players = List(player),
        servers = List(),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      val command = MovePlayerCommand(0, tile2)
      val movedGame = command.doStep(game).get
      val undoneGame = command.undoStep(movedGame).get

      val finalPlayer = undoneGame.model.players(0)
      finalPlayer.tile shouldBe tile1
      finalPlayer.movementPoints shouldBe 5
    }

    "fail if player doesn't have enough movement points" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val tile2 = Tile(5, 5, Continent.Europe) // Distance: 10
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 3
      )

      val model = GameModel(
        players = List(player),
        servers = List(),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      val command = MovePlayerCommand(0, tile2)
      val result = command.doStep(game)

      result.isFailure shouldBe true
      result.failed.get.getMessage should include("Nicht genug Bewegungspunkte")
    }

    "fail if trying to move to ocean tile" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val oceanTile = Tile(0, 1, Continent.Ocean)
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val model = GameModel(
        players = List(player),
        servers = List(),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      val command = MovePlayerCommand(0, oceanTile)
      val result = command.doStep(game)

      result.isFailure shouldBe true
      result.failed.get.getMessage should include("nicht auf Ocean-Tile")
    }

    "fail with invalid player index" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val tile2 = Tile(1, 1, Continent.Europe)
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val model = GameModel(
        players = List(player),
        servers = List(),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      val command = MovePlayerCommand(5, tile2)
      val result = command.doStep(game)

      result.isFailure shouldBe true
      result.failed.get.getMessage should include("Ungültiger Spieler-Index")
    }

    "calculate Manhattan distance correctly" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val tile2 = Tile(3, 4, Continent.Europe) // Distance: 3 + 4 = 7
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 10
      )

      val model = GameModel(
        players = List(player),
        servers = List(),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      val command = MovePlayerCommand(0, tile2)
      val result = command.doStep(game)

      result.isSuccess shouldBe true
      val movedPlayer = result.get.model.players(0)
      movedPlayer.movementPoints shouldBe 3 // 10 - 7 = 3
    }

    "automatically hack a server when moving onto it (successful hack)" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val serverTile = Tile(1, 0, Continent.Europe) // Distance: 1
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 50,
        ram = 50,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 5,
        rewardRam = 3,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      // Use fixed Random to ensure success
      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 0 // Always succeed
      }

      val command = MovePlayerCommand(0, serverTile, fixedRandom)
      val result = command.doStep(game)

      result.isSuccess shouldBe true
      val newGame = result.get
      val movedPlayer = newGame.model.players(0)
      val hackedServer = newGame.model.servers(0)

      movedPlayer.tile shouldBe serverTile
      movedPlayer.movementPoints shouldBe 4 // 5 - 1 = 4
      movedPlayer.cpu should be > (player.cpu - 50) // Got rewards
      movedPlayer.xp should be > player.xp // Got XP
      hackedServer.hacked shouldBe true
      hackedServer.hackedBy shouldBe Some(0)
    }

    "automatically hack a server when moving onto it (failed hack)" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val serverTile = Tile(1, 0, Continent.Europe) // Distance: 1
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 50,
        ram = 50,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 5,
        rewardRam = 3,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      // Use fixed Random to ensure failure
      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 99 // Always fail
      }

      val command = MovePlayerCommand(0, serverTile, fixedRandom)
      val result = command.doStep(game)

      result.isSuccess shouldBe true
      val newGame = result.get
      val movedPlayer = newGame.model.players(0)
      val hackedServer = newGame.model.servers(0)

      movedPlayer.tile shouldBe serverTile
      movedPlayer.cpu should be < player.cpu // Lost CPU
      movedPlayer.ram should be < player.ram // Lost RAM
      movedPlayer.xp shouldBe player.xp // No XP gained
      hackedServer.hacked shouldBe false
    }

    "not hack a server if player doesn't have enough resources" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val serverTile = Tile(1, 0, Continent.Europe)
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 1,
        ram = 1,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 50,
        rewardCpu = 5,
        rewardRam = 3,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      val command = MovePlayerCommand(0, serverTile)
      val result = command.doStep(game)

      result.isSuccess shouldBe true
      val newGame = result.get
      val movedPlayer = newGame.model.players(0)
      val serverAfter = newGame.model.servers(0)

      movedPlayer.tile shouldBe serverTile
      movedPlayer.cpu shouldBe 1 // Unchanged
      movedPlayer.ram shouldBe 1 // Unchanged
      serverAfter.hacked shouldBe false
    }

    "not hack an already hacked server" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val serverTile = Tile(1, 0, Continent.Europe)
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 50,
        ram = 50,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 5,
        rewardRam = 3,
        hacked = true,
        serverType = ServerType.Firm,
        hackedBy = Some(0)
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      val command = MovePlayerCommand(0, serverTile)
      val result = command.doStep(game)

      result.isSuccess shouldBe true
      val newGame = result.get
      val movedPlayer = newGame.model.players(0)

      movedPlayer.tile shouldBe serverTile
      movedPlayer.cpu shouldBe 50 // Unchanged (no hack attempt)
      movedPlayer.ram shouldBe 50 // Unchanged
    }

    "not hack a Private server" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val serverTile = Tile(1, 0, Continent.Europe)
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 50,
        ram = 50,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 5,
        rewardRam = 3,
        hacked = false,
        serverType = ServerType.Private
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      val command = MovePlayerCommand(0, serverTile)
      val result = command.doStep(game)

      result.isSuccess shouldBe true
      val newGame = result.get
      val movedPlayer = newGame.model.players(0)

      movedPlayer.tile shouldBe serverTile
      movedPlayer.cpu shouldBe 50 // Unchanged (no hack attempt on Private)
      movedPlayer.ram shouldBe 50 // Unchanged
    }

    "undo a successful server hack correctly" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val serverTile = Tile(1, 0, Continent.Europe)
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 50,
        ram = 50,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 5,
        rewardRam = 3,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      // Use fixed Random to ensure success
      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 0
      }

      val command = MovePlayerCommand(0, serverTile, fixedRandom)
      val movedGame = command.doStep(game).get
      val undoneGame = command.undoStep(movedGame).get

      val finalPlayer = undoneGame.model.players(0)
      val finalServer = undoneGame.model.servers(0)

      finalPlayer.tile shouldBe tile1
      finalPlayer.movementPoints shouldBe 5
      finalPlayer.cpu shouldBe 50
      finalPlayer.ram shouldBe 50
      finalPlayer.xp shouldBe 0
      finalServer.hacked shouldBe false
      finalServer.hackedBy shouldBe None
    }

    "handle different server types with correct rewards" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val serverTile = Tile(1, 0, Continent.Europe)
      val player = Player(
        id = 0,
        name = "Tester",
        tile = tile1,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val bankServer = Server(
        name = "BankServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Bank
      )

      val model = GameModel(
        players = List(player),
        servers = List(bankServer),
        worldMap = WorldMap.defaultMap
      )
      val game = Game(model, GameState())

      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 0 // Always succeed
      }

      val command = MovePlayerCommand(0, serverTile, fixedRandom)
      val result = command.doStep(game)

      result.isSuccess shouldBe true
      val movedPlayer = result.get.model.players(0)

      // Bank gives code reward instead of CPU/RAM
      movedPlayer.code should be > player.code
      movedPlayer.xp shouldBe 40 // Bank gives 40 XP
    }
  }
}
