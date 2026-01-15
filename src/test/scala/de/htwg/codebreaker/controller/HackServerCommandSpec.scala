package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game.{Game, GameModel, GameState}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class HackServerCommandSpec extends AnyWordSpec with Matchers {

  "A HackServerCommand" should {

    "successfully hack a server when conditions are met" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 20,
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      // Use fixed Random to ensure success
      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 0 // Always succeed
      }

      val command = HackServerCommand("TestServer", 0, fixedRandom)
      val result  = command.doStep(game)

      result.isSuccess shouldBe true
      val newGame      = result.get
      val hackedPlayer = newGame.model.players(0)
      val hackedServer = newGame.model.servers(0)

      // Player should have gained rewards
      hackedPlayer.cpu should be > (player.cpu - 50) // Lost some CPU but gained rewards
      hackedPlayer.xp shouldBe 20                    // Firm gives 20 XP

      // Server should be hacked
      hackedServer.hacked shouldBe true
      hackedServer.hackedBy shouldBe Some(0)
      hackedServer.claimedBy shouldBe Some(0)
    }

    "fail to hack when resources are consumed even on failure" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 20,
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      // Use fixed Random to ensure failure
      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 99 // Always fail
      }

      val command = HackServerCommand("TestServer", 0, fixedRandom)
      val result  = command.doStep(game)

      result.isSuccess shouldBe true
      val newGame      = result.get
      val hackedPlayer = newGame.model.players(0)
      val hackedServer = newGame.model.servers(0)

      // Player should have lost resources
      hackedPlayer.cpu should be < player.cpu
      hackedPlayer.ram should be < player.ram
      hackedPlayer.xp shouldBe 0 // No XP on failure

      // Server should NOT be hacked
      hackedServer.hacked shouldBe false
      hackedServer.hackedBy shouldBe None
    }

    "undo a successful hack correctly" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 20,
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 0
      }

      val command    = HackServerCommand("TestServer", 0, fixedRandom)
      val hackedGame = command.doStep(game).get
      val undoneGame = command.undoStep(hackedGame).get

      val finalPlayer = undoneGame.model.players(0)
      val finalServer = undoneGame.model.servers(0)

      // Player should be back to original state
      finalPlayer.cpu shouldBe player.cpu
      finalPlayer.ram shouldBe player.ram
      finalPlayer.xp shouldBe player.xp

      // Server should be unhacked
      finalServer.hacked shouldBe false
      finalServer.hackedBy shouldBe None
    }

    "undo a failed hack correctly" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 20,
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 99 // Fail
      }

      val command    = HackServerCommand("TestServer", 0, fixedRandom)
      val hackedGame = command.doStep(game).get
      val undoneGame = command.undoStep(hackedGame).get

      val finalPlayer = undoneGame.model.players(0)
      val finalServer = undoneGame.model.servers(0)

      // Player should be restored
      finalPlayer.cpu shouldBe player.cpu
      finalPlayer.ram shouldBe player.ram

      // Server should still be unhacked
      finalServer.hacked shouldBe false
    }

    "fail if server name doesn't exist" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
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
      val game  = Game(model, GameState())

      val command = HackServerCommand("NonExistent", 0)
      val result  = command.doStep(game)

      result.isFailure shouldBe true
      result.failed.get.getMessage should include("nicht gefunden")
    }

    "fail if player is not on server tile" in {
      val playerTile = Tile(0, 0, Continent.Europe)
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = playerTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 20,
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val command = HackServerCommand("TestServer", 0)
      val result  = command.doStep(game)

      result.isFailure shouldBe true
      result.failed.get.getMessage should include("muss auf Server-Tile sein")
    }

    "fail if server is already hacked" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 20,
        rewardCpu = 10,
        rewardRam = 5,
        hacked = true,
        serverType = ServerType.Firm,
        hackedBy = Some(1)
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val command = HackServerCommand("TestServer", 0)
      val result  = command.doStep(game)

      result.isFailure shouldBe true
      result.failed.get.getMessage should include("bereits gehackt")
    }

    "fail if server is Private" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "PrivateServer",
        tile = serverTile,
        difficulty = 20,
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Private
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val command = HackServerCommand("PrivateServer", 0)
      val result  = command.doStep(game)

      result.isFailure shouldBe true
      result.failed.get.getMessage should include("Private Server")
    }

    "fail if player doesn't have enough CPU" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 1,
        ram = 100,
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
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val command = HackServerCommand("TestServer", 0)
      val result  = command.doStep(game)

      result.isFailure shouldBe true
      result.failed.get.getMessage should include("Nicht genug CPU")
    }

    "fail if player doesn't have enough RAM" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
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
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val command = HackServerCommand("TestServer", 0)
      val result  = command.doStep(game)

      result.isFailure shouldBe true
      result.failed.get.getMessage should include("Nicht genug RAM")
    }

    "give correct rewards for Side server" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "SideServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 5,
        rewardRam = 3,
        hacked = false,
        serverType = ServerType.Side
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 0
      }

      val command = HackServerCommand("SideServer", 0, fixedRandom)
      val result  = command.doStep(game).get

      val hackedPlayer = result.model.players(0)
      hackedPlayer.xp shouldBe 10 // Side gives 10 XP
    }

    "give correct rewards for Cloud server" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "CloudServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 8,
        rewardRam = 6,
        hacked = false,
        serverType = ServerType.Cloud
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 0
      }

      val command = HackServerCommand("CloudServer", 0, fixedRandom)
      val result  = command.doStep(game).get

      val hackedPlayer = result.model.players(0)
      hackedPlayer.xp shouldBe 30 // Cloud gives 30 XP
    }

    "give correct rewards for Bank server (code instead of CPU/RAM)" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "BankServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 15,
        rewardRam = 10,
        hacked = false,
        serverType = ServerType.Bank
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 0
      }

      val command = HackServerCommand("BankServer", 0, fixedRandom)
      val result  = command.doStep(game).get

      val hackedPlayer = result.model.players(0)
      hackedPlayer.code shouldBe 15 // Bank gives code reward
      hackedPlayer.xp shouldBe 40   // Bank gives 40 XP
    }

    "give correct rewards for Military server (double rewards)" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "MilitaryServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 10,
        rewardRam = 8,
        hacked = false,
        serverType = ServerType.Military
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 0
      }

      val command = HackServerCommand("MilitaryServer", 0, fixedRandom)
      val result  = command.doStep(game).get

      val hackedPlayer = result.model.players(0)
      val cpuCost      = 10 / 2
      val ramCost      = 10 / 3

      // Military gives double rewards
      hackedPlayer.cpu shouldBe (100 - cpuCost + 20) // Lost cost, gained 2x reward
      hackedPlayer.ram shouldBe (100 - ramCost + 16) // Lost cost, gained 2x reward
      hackedPlayer.xp shouldBe 50                    // Military gives 50 XP
    }

    "give correct rewards for GKS server (high XP)" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "GKSServer",
        tile = serverTile,
        difficulty = 10,
        rewardCpu = 10,
        rewardRam = 8,
        hacked = false,
        serverType = ServerType.GKS
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      val fixedRandom = new Random() {
        override def nextInt(n: Int): Int = 0
      }

      val command = HackServerCommand("GKSServer", 0, fixedRandom)
      val result  = command.doStep(game).get

      val hackedPlayer = result.model.players(0)
      hackedPlayer.xp shouldBe 100 // GKS gives 100 XP (end goal)
    }

    "calculate success chance correctly with cybersecurity bonus" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 40, // +20% success chance
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 50, // 50% base chance
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      // With 50 difficulty and 40 cybersecurity:
      // baseChance = 100 - 50 = 50
      // securityBonus = 40 / 2 = 20
      // successChance = 50 + 20 = 70%
      // Roll 69 should succeed, 70 should fail

      val successRandom  = new Random() {
        override def nextInt(n: Int): Int = 69
      }
      val successCommand = HackServerCommand("TestServer", 0, successRandom)
      val successResult  = successCommand.doStep(game).get
      successResult.model.servers(0).hacked shouldBe true

      val failRandom  = new Random() {
        override def nextInt(n: Int): Int = 70
      }
      val failCommand = HackServerCommand("TestServer", 0, failRandom)
      val failResult  = failCommand.doStep(game).get
      failResult.model.servers(0).hacked shouldBe false
    }

    "cap success chance at 95%" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 200, // Very high cybersecurity
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 1, // Very easy server
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      // Should cap at 95%
      val failRandom = new Random() {
        override def nextInt(n: Int): Int = 95 // Should fail
      }
      val command    = HackServerCommand("TestServer", 0, failRandom)
      val result     = command.doStep(game).get
      result.model.servers(0).hacked shouldBe false
    }

    "cap success chance at 5%" in {
      val serverTile = Tile(5, 5, Continent.Europe)
      val player     = Player(
        id = 0,
        name = "Hacker",
        tile = serverTile,
        cpu = 100,
        ram = 100,
        code = 0,
        level = 1,
        xp = 0,
        cybersecurity = 0,
        movementPoints = 5
      )

      val server = Server(
        name = "TestServer",
        tile = serverTile,
        difficulty = 99, // Nearly impossible
        rewardCpu = 10,
        rewardRam = 5,
        hacked = false,
        serverType = ServerType.Firm
      )

      val model = GameModel(
        players = List(player),
        servers = List(server),
        worldMap = WorldMap.defaultMap
      )
      val game  = Game(model, GameState())

      // Should have at least 5% chance
      val successRandom = new Random() {
        override def nextInt(n: Int): Int = 4 // Should succeed
      }
      val command       = HackServerCommand("TestServer", 0, successRandom)
      val result        = command.doStep(game).get
      result.model.servers(0).hacked shouldBe true
    }
  }
}
