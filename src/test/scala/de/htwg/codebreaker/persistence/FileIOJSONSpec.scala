package de.htwg.codebreaker.persistence

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import scala.util.{Success, Failure}
import java.io.File

class FileIOJSONSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  val fileIO = new FileIOJSON()
  val filePath = "game_save.json"

  // Test data setup
  val tile1 = Tile(0, 0, Continent.Europe)
  val tile2 = Tile(5, 5, Continent.Asia)

  val player1 = Player(
    id = 0,
    name = "Alice",
    tile = tile1,
    cpu = 100,
    ram = 100,
    code = 1,
    level = 1,
    xp = 50,
    cybersecurity = 10,
    movementPoints = 5,
    maxMovementPoints = 5
  )

  val player2 = Player(
    id = 1,
    name = "Bob",
    tile = tile2,
    cpu = 80,
    ram = 90,
    code = 2,
    level = 2,
    xp = 120,
    cybersecurity = 15,
    movementPoints = 3,
    maxMovementPoints = 5
  )

  val server1 = Server(
    name = "Server1",
    tile = tile1,
    difficulty = 20,
    rewardCpu = 5,
    rewardRam = 5,
    hacked = false,
    serverType = ServerType.Bank,
    hackedBy = None
  )

  val server2 = Server(
    name = "Server2",
    tile = tile2,
    difficulty = 30,
    rewardCpu = 10,
    rewardRam = 10,
    hacked = true,
    serverType = ServerType.Firm,
    hackedBy = Some(0)
  )

  val worldMap = WorldMap.defaultMap
  val model = GameModel(List(player1, player2), List(server1, server2), worldMap)
  val state = GameState(
    currentPlayerIndex = Some(0),
    status = GameStatus.Running,
    phase = Phase.AwaitingInput,
    round = 1
  )
  val testGame = Game(model, state)

  override def afterEach(): Unit = {
    // Clean up test file after each test
    val file = new File(filePath)
    if (file.exists()) {
      file.delete()
    }
  }

  "FileIOJSON" should {

    "save a game successfully" in {
      val result = fileIO.save(testGame)
      result shouldBe a[Success[?]]

      // Verify file was created
      val file = new File(filePath)
      file.exists() shouldBe true
    }

    "load a previously saved game" in {
      // First save a game
      fileIO.save(testGame)

      // Then load it
      val result = fileIO.load()
      result shouldBe a[Success[?]]

      val loadedGame = result.get
      loadedGame shouldBe testGame
    }

    "perform a successful roundtrip (save and load)" in {
      // Save
      fileIO.save(testGame) shouldBe a[Success[?]]

      // Load
      val loadedGame = fileIO.load().get

      // Verify all components match
      loadedGame.model.players.size shouldBe 2
      loadedGame.model.servers.size shouldBe 2
      loadedGame.state.currentPlayerIndex shouldBe Some(0)
      loadedGame.state.round shouldBe 1
      loadedGame.state.status shouldBe GameStatus.Running

      // Verify player details
      val loadedPlayer1 = loadedGame.model.players(0)
      loadedPlayer1.name shouldBe "Alice"
      loadedPlayer1.cpu shouldBe 100
      loadedPlayer1.tile shouldBe tile1

      // Verify server details
      val loadedServer2 = loadedGame.model.servers(1)
      loadedServer2.name shouldBe "Server2"
      loadedServer2.hacked shouldBe true
      loadedServer2.hackedBy shouldBe Some(0)
    }

    "fail when loading from non-existent file" in {
      // Make sure file doesn't exist
      val file = new File(filePath)
      if (file.exists()) file.delete()

      val result = fileIO.load()
      result shouldBe a[Failure[?]]
    }

    "save and load game with no current player" in {
      val stateNoPlayer = state.copy(currentPlayerIndex = None)
      val gameNoPlayer = testGame.copy(state = stateNoPlayer)

      fileIO.save(gameNoPlayer) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.state.currentPlayerIndex shouldBe None
    }

    "save and load game with different game statuses" in {
      val gameOverState = state.copy(status = GameStatus.GameOver)
      val gameOverGame = testGame.copy(state = gameOverState)

      fileIO.save(gameOverGame) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.state.status shouldBe GameStatus.GameOver
    }

    "save and load game with different phases" in {
      val execState = state.copy(phase = Phase.FinishedTurn)
      val execGame = testGame.copy(state = execState)

      fileIO.save(execGame) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.state.phase shouldBe Phase.FinishedTurn
    }

    "save and load game with all server types" in {
      val servers = List(
        Server("Bank", tile1, 10, 1, 1, false, ServerType.Bank),
        Server("Firm", tile1, 10, 1, 1, false, ServerType.Firm),
        Server("Side", tile1, 10, 1, 1, false, ServerType.Side),
        Server("Cloud", tile1, 10, 1, 1, false, ServerType.Cloud),
        Server("Military", tile1, 10, 1, 1, false, ServerType.Military),
        Server("GKS", tile1, 10, 1, 1, false, ServerType.GKS),
        Server("Private", tile1, 10, 1, 1, false, ServerType.Private)
      )
      val modelWithAllTypes = model.copy(servers = servers)
      val gameWithAllTypes = testGame.copy(model = modelWithAllTypes)

      fileIO.save(gameWithAllTypes) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.model.servers.size shouldBe 7
      loaded.model.servers.map(_.serverType) should contain allOf(
        ServerType.Bank,
        ServerType.Firm,
        ServerType.Side,
        ServerType.Cloud,
        ServerType.Military,
        ServerType.GKS,
        ServerType.Private
      )
    }

    "save and load game with all continents" in {
      val tiles = List(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Asia),
        Tile(2, 0, Continent.Africa),
        Tile(3, 0, Continent.NorthAmerica),
        Tile(4, 0, Continent.SouthAmerica),
        Tile(5, 0, Continent.Oceania),
        Tile(6, 0, Continent.Ocean)
      )
      val players = tiles.zipWithIndex.take(6).map { case (tile, idx) =>
        player1.copy(id = idx, tile = tile)
      }.toList

      val modelWithAllContinents = model.copy(players = players)
      val gameWithAllContinents = testGame.copy(model = modelWithAllContinents)

      fileIO.save(gameWithAllContinents) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.model.players.size shouldBe 6
      loaded.model.players.map(_.tile.continent) should contain allOf(
        Continent.Europe,
        Continent.Asia,
        Continent.Africa,
        Continent.NorthAmerica,
        Continent.SouthAmerica,
        Continent.Oceania
      )
    }

    "save and load empty player list" in {
      val emptyModel = model.copy(players = List.empty)
      val emptyGame = testGame.copy(model = emptyModel)

      fileIO.save(emptyGame) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.model.players shouldBe empty
    }

    "save and load empty server list" in {
      val emptyModel = model.copy(servers = List.empty)
      val emptyGame = testGame.copy(model = emptyModel)

      fileIO.save(emptyGame) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.model.servers shouldBe empty
    }

    "preserve player stats correctly" in {
      val playerWithStats = player1.copy(
        cpu = 42,
        ram = 73,
        code = 5,
        level = 10,
        xp = 9999,
        cybersecurity = 85,
        movementPoints = 2,
        maxMovementPoints = 8
      )
      val modelWithStats = model.copy(players = List(playerWithStats))
      val gameWithStats = testGame.copy(model = modelWithStats)

      fileIO.save(gameWithStats) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedPlayer = loaded.model.players.head

      loadedPlayer.cpu shouldBe 42
      loadedPlayer.ram shouldBe 73
      loadedPlayer.code shouldBe 5
      loadedPlayer.level shouldBe 10
      loadedPlayer.xp shouldBe 9999
      loadedPlayer.cybersecurity shouldBe 85
      loadedPlayer.movementPoints shouldBe 2
      loadedPlayer.maxMovementPoints shouldBe 8
    }

    "preserve server rewards correctly" in {
      val serverWithRewards = server1.copy(
        rewardCpu = 25,
        rewardRam = 30
      )
      val modelWithRewards = model.copy(servers = List(serverWithRewards))
      val gameWithRewards = testGame.copy(model = modelWithRewards)

      fileIO.save(gameWithRewards) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.rewardCpu shouldBe 25
      loadedServer.rewardRam shouldBe 30
    }
  }
}
