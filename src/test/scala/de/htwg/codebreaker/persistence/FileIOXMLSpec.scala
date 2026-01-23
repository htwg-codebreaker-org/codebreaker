package de.htwg.codebreaker.persistence

import de.htwg.codebreaker.persistence.XML.FileIOXML
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.model.map.{Tile, Continent, WorldMap}
import de.htwg.codebreaker.model.player.{Player}
import de.htwg.codebreaker.model.player.skill.PlayerSkillTree
import de.htwg.codebreaker.model.player.laptop.{Laptop, LaptopHardware, LaptopInstalledTools}
import de.htwg.codebreaker.model.server.{Server, ServerType}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import scala.util.{Success, Failure}
import java.io.File

class FileIOXMLSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  val fileIO = new FileIOXML()
  val filePath = "game_save.xml"

  // Test data setup
  val tile1 = Tile(0, 0, Continent.Europe)
  val tile2 = Tile(5, 5, Continent.Asia)

  val hardware1 = LaptopHardware(cpu = 100, ram = 100, code = 1, kerne = 1, networkRange = 1)
  val tools1 = LaptopInstalledTools.empty
  val laptop1 = Laptop(hardware = hardware1, tools = tools1, runningActions = Nil, runningInternetSearch = None, cybersecurity = 10)

  val player1 = Player(
    id = 0,
    name = "Alice",
    tile = tile1,
    laptop = laptop1,
    availableXp = 50,
    totalXpEarned = 50,
    skills = PlayerSkillTree(Set.empty),
    arrested = false,
    movementPoints = 5,
    maxMovementPoints = 5
  )

  val hardware2 = LaptopHardware(cpu = 80, ram = 90, code = 2, kerne = 1, networkRange = 1)
  val tools2 = LaptopInstalledTools.empty
  val laptop2 = Laptop(hardware = hardware2, tools = tools2, runningActions = Nil, runningInternetSearch = None, cybersecurity = 15)

  val player2 = Player(
    id = 1,
    name = "Bob",
    tile = tile2,
    laptop = laptop2,
    availableXp = 120,
    totalXpEarned = 120,
    skills = PlayerSkillTree(Set.empty),
    arrested = false,
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
    hackedBy = None,
    claimedBy = None,
    cybersecurityLevel = 10,
    blockedUntilRound = None,
    installedRole = None
  )

  val server2 = Server(
    name = "Server2",
    tile = tile2,
    difficulty = 30,
    rewardCpu = 10,
    rewardRam = 10,
    hacked = true,
    serverType = ServerType.Firm,
    hackedBy = Some(0),
    claimedBy = None,
    cybersecurityLevel = 10,
    blockedUntilRound = None,
    installedRole = None
  )

  val worldMap = WorldMap.defaultMap
  val model = GameModel(List(player1, player2), List(server1, server2), worldMap, Nil, Nil, Nil, Nil, Nil)
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

  "FileIOXML" should {

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
      loadedPlayer1.laptop.hardware.cpu shouldBe 100
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
      val finishedState = state.copy(phase = Phase.FinishedTurn)
      val finishedGame = testGame.copy(state = finishedState)

      fileIO.save(finishedGame) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.state.phase shouldBe Phase.FinishedTurn
    }

    "save and load game with all server types" in {
      val servers = List(
        Server("Bank", tile1, 10, 1, 1, false, ServerType.Bank, None, None, 10, None, None),
        Server("Firm", tile1, 10, 1, 1, false, ServerType.Firm, None, None, 10, None, None),
        Server("Side", tile1, 10, 1, 1, false, ServerType.Side, None, None, 10, None, None),
        Server("Cloud", tile1, 10, 1, 1, false, ServerType.Cloud, None, None, 10, None, None),
        Server("Military", tile1, 10, 1, 1, false, ServerType.Military, None, None, 10, None, None),
        Server("GKS", tile1, 10, 1, 1, false, ServerType.GKS, None, None, 10, None, None),
        Server("Private", tile1, 10, 1, 1, false, ServerType.Private, None, None, 10, None, None)
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
      val hardwareWithStats = LaptopHardware(cpu = 42, ram = 73, code = 5, kerne = 1, networkRange = 1)
      val laptopWithStats = laptop1.copy(hardware = hardwareWithStats, cybersecurity = 85)
      val playerWithStats = player1.copy(
        laptop = laptopWithStats,
        availableXp = 150,
        totalXpEarned = 9999,
        movementPoints = 2,
        maxMovementPoints = 8
      )
      val modelWithStats = model.copy(players = List(playerWithStats))
      val gameWithStats = testGame.copy(model = modelWithStats)

      fileIO.save(gameWithStats) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedPlayer = loaded.model.players.head

      loadedPlayer.laptop.hardware.cpu shouldBe 42
      loadedPlayer.laptop.hardware.ram shouldBe 73
      loadedPlayer.laptop.hardware.code shouldBe 5
      loadedPlayer.availableXp shouldBe 150
      loadedPlayer.totalXpEarned shouldBe 9999
      loadedPlayer.laptop.cybersecurity shouldBe 85
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

    "save XML with proper encoding header" in {
      fileIO.save(testGame) shouldBe a[Success[?]]

      val file = new File(filePath)
      val source = scala.io.Source.fromFile(file)
      val firstLine = try {
        source.getLines().next()
      } finally {
        source.close()
      }

      firstLine should include("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    }

    "handle server with hackedBy as None correctly" in {
      val serverNotHacked = server1.copy(hacked = false, hackedBy = None)
      val modelWithServer = model.copy(servers = List(serverNotHacked))
      val gameWithServer = testGame.copy(model = modelWithServer)

      fileIO.save(gameWithServer) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.hacked shouldBe false
      loadedServer.hackedBy shouldBe None
    }

    "handle server with hackedBy as Some correctly" in {
      val serverHacked = server2.copy(hacked = true, hackedBy = Some(1))
      val modelWithServer = model.copy(servers = List(serverHacked))
      val gameWithServer = testGame.copy(model = modelWithServer)

      fileIO.save(gameWithServer) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.hacked shouldBe true
      loadedServer.hackedBy shouldBe Some(1)
    }

    "save and load player with arrested status" in {
      val arrestedPlayer = player1.copy(arrested = true)
      val modelWithArrested = model.copy(players = List(arrestedPlayer))
      val gameWithArrested = testGame.copy(model = modelWithArrested)

      fileIO.save(gameWithArrested) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedPlayer = loaded.model.players.head

      loadedPlayer.arrested shouldBe true
    }

    "save and load server with claimedBy" in {
      val claimedServer = server1.copy(claimedBy = Some(1))
      val modelWithClaimed = model.copy(servers = List(claimedServer))
      val gameWithClaimed = testGame.copy(model = modelWithClaimed)

      fileIO.save(gameWithClaimed) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.claimedBy shouldBe Some(1)
    }

    "save and load server with blockedUntilRound" in {
      val blockedServer = server1.copy(blockedUntilRound = Some(10))
      val modelWithBlocked = model.copy(servers = List(blockedServer))
      val gameWithBlocked = testGame.copy(model = modelWithBlocked)

      fileIO.save(gameWithBlocked) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.blockedUntilRound shouldBe Some(10)
    }

    "save and load player with unlocked hack skills" in {
      val skillTree = PlayerSkillTree(unlockedHackSkills = Set("bruteforce", "exploit", "ddos"))
      val playerWithSkills = player1.copy(skills = skillTree)
      val modelWithSkills = model.copy(players = List(playerWithSkills))
      val gameWithSkills = testGame.copy(model = modelWithSkills)

      fileIO.save(gameWithSkills) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedPlayer = loaded.model.players.head

      loadedPlayer.skills.unlockedHackSkills should contain allOf("bruteforce", "exploit", "ddos")
      loadedPlayer.skills.unlockedHackSkills.size shouldBe 3
    }

    "save and load player with unlocked social skills" in {
      val skillTree = PlayerSkillTree(unlockedSocialSkills = Set("phishing", "socialengineering"))
      val playerWithSkills = player1.copy(skills = skillTree)
      val modelWithSkills = model.copy(players = List(playerWithSkills))
      val gameWithSkills = testGame.copy(model = modelWithSkills)

      fileIO.save(gameWithSkills) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedPlayer = loaded.model.players.head

      loadedPlayer.skills.unlockedSocialSkills should contain allOf("phishing", "socialengineering")
      loadedPlayer.skills.unlockedSocialSkills.size shouldBe 2
    }

    "save and load player with both hack and social skills" in {
      val skillTree = PlayerSkillTree(
        unlockedHackSkills = Set("bruteforce", "exploit"),
        unlockedSocialSkills = Set("phishing")
      )
      val playerWithSkills = player1.copy(skills = skillTree)
      val modelWithSkills = model.copy(players = List(playerWithSkills))
      val gameWithSkills = testGame.copy(model = modelWithSkills)

      fileIO.save(gameWithSkills) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedPlayer = loaded.model.players.head

      loadedPlayer.skills.unlockedHackSkills should contain allOf("bruteforce", "exploit")
      loadedPlayer.skills.unlockedSocialSkills should contain("phishing")
    }

    "save and load game with all phases" in {
      val phases = List(Phase.AwaitingInput, Phase.ExecutingTurn, Phase.FinishedTurn)

      phases.foreach { phase =>
        val stateWithPhase = state.copy(phase = phase)
        val gameWithPhase = testGame.copy(state = stateWithPhase)

        fileIO.save(gameWithPhase) shouldBe a[Success[?]]
        val loaded = fileIO.load().get

        loaded.state.phase shouldBe phase
      }
    }

    "save and load game with all game statuses" in {
      val statuses = List(GameStatus.Running, GameStatus.Paused, GameStatus.GameOver)

      statuses.foreach { status =>
        val stateWithStatus = state.copy(status = status)
        val gameWithStatus = testGame.copy(state = stateWithStatus)

        fileIO.save(gameWithStatus) shouldBe a[Success[?]]
        val loaded = fileIO.load().get

        loaded.state.status shouldBe status
      }
    }

    "preserve round number correctly" in {
      val stateWithRound = state.copy(round = 42)
      val gameWithRound = testGame.copy(state = stateWithRound)

      fileIO.save(gameWithRound) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.state.round shouldBe 42
    }

    "preserve current player index correctly" in {
      val stateWithIndex = state.copy(currentPlayerIndex = Some(1))
      val gameWithIndex = testGame.copy(state = stateWithIndex)

      fileIO.save(gameWithIndex) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.state.currentPlayerIndex shouldBe Some(1)
    }

    "preserve server cybersecurity level" in {
      val serverWithSecurity = server1.copy(cybersecurityLevel = 99)
      val modelWithSecurity = model.copy(servers = List(serverWithSecurity))
      val gameWithSecurity = testGame.copy(model = modelWithSecurity)

      fileIO.save(gameWithSecurity) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.cybersecurityLevel shouldBe 99
    }

    "preserve server difficulty correctly" in {
      val serverWithDifficulty = server1.copy(difficulty = 75)
      val modelWithDifficulty = model.copy(servers = List(serverWithDifficulty))
      val gameWithDifficulty = testGame.copy(model = modelWithDifficulty)

      fileIO.save(gameWithDifficulty) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.difficulty shouldBe 75
    }

    "handle multiple players with different tiles" in {
      val tiles = List(
        Tile(0, 0, Continent.Europe),
        Tile(10, 20, Continent.Asia),
        Tile(5, 15, Continent.Africa)
      )
      val players = tiles.zipWithIndex.map { case (tile, idx) =>
        player1.copy(id = idx, name = s"Player$idx", tile = tile)
      }
      val modelWithPlayers = model.copy(players = players)
      val gameWithPlayers = testGame.copy(model = modelWithPlayers)

      fileIO.save(gameWithPlayers) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.model.players.size shouldBe 3
      loaded.model.players(0).tile shouldBe Tile(0, 0, Continent.Europe)
      loaded.model.players(1).tile shouldBe Tile(10, 20, Continent.Asia)
      loaded.model.players(2).tile shouldBe Tile(5, 15, Continent.Africa)
    }

    "handle laptop hardware with different configurations" in {
      val hardware = LaptopHardware(cpu = 200, ram = 300, code = 10, kerne = 4, networkRange = 5)
      val laptop = laptop1.copy(hardware = hardware)
      val player = player1.copy(laptop = laptop)
      val modelWithHardware = model.copy(players = List(player))
      val gameWithHardware = testGame.copy(model = modelWithHardware)

      fileIO.save(gameWithHardware) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedPlayer = loaded.model.players.head

      loadedPlayer.laptop.hardware.cpu shouldBe 200
      loadedPlayer.laptop.hardware.ram shouldBe 300
      loadedPlayer.laptop.hardware.code shouldBe 10
      loadedPlayer.laptop.hardware.kerne shouldBe 4
      loadedPlayer.laptop.hardware.networkRange shouldBe 5
    }

    "preserve player movement points" in {
      val playerWithMovement = player1.copy(movementPoints = 2, maxMovementPoints = 7)
      val modelWithMovement = model.copy(players = List(playerWithMovement))
      val gameWithMovement = testGame.copy(model = modelWithMovement)

      fileIO.save(gameWithMovement) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedPlayer = loaded.model.players.head

      loadedPlayer.movementPoints shouldBe 2
      loadedPlayer.maxMovementPoints shouldBe 7
    }

    "handle server with all optional fields as None" in {
      val serverMinimal = Server(
        name = "Minimal",
        tile = tile1,
        difficulty = 10,
        rewardCpu = 1,
        rewardRam = 1,
        hacked = false,
        serverType = ServerType.Side,
        hackedBy = None,
        claimedBy = None,
        cybersecurityLevel = 5,
        blockedUntilRound = None,
        installedRole = None
      )
      val modelWithMinimal = model.copy(servers = List(serverMinimal))
      val gameWithMinimal = testGame.copy(model = modelWithMinimal)

      fileIO.save(gameWithMinimal) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.hackedBy shouldBe None
      loadedServer.claimedBy shouldBe None
      loadedServer.blockedUntilRound shouldBe None
      loadedServer.installedRole shouldBe None
    }

    "handle server with all optional fields as Some" in {
      val serverMaximal = server1.copy(
        hacked = true,
        hackedBy = Some(0),
        claimedBy = Some(1),
        blockedUntilRound = Some(20)
      )
      val modelWithMaximal = model.copy(servers = List(serverMaximal))
      val gameWithMaximal = testGame.copy(model = modelWithMaximal)

      fileIO.save(gameWithMaximal) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.hacked shouldBe true
      loadedServer.hackedBy shouldBe Some(0)
      loadedServer.claimedBy shouldBe Some(1)
      loadedServer.blockedUntilRound shouldBe Some(20)
    }

    "preserve WorldMap dimensions" in {
      fileIO.save(testGame) shouldBe a[Success[?]]
      val loaded = fileIO.load().get

      loaded.model.map.width shouldBe worldMap.width
      loaded.model.map.height shouldBe worldMap.height
    }

    "handle claimedBy as None correctly" in {
      val serverNotClaimed = server1.copy(claimedBy = None)
      val modelWithServer = model.copy(servers = List(serverNotClaimed))
      val gameWithServer = testGame.copy(model = modelWithServer)

      fileIO.save(gameWithServer) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.claimedBy shouldBe None
    }

    "handle blockedUntilRound as None correctly" in {
      val serverNotBlocked = server1.copy(blockedUntilRound = None)
      val modelWithServer = model.copy(servers = List(serverNotBlocked))
      val gameWithServer = testGame.copy(model = modelWithServer)

      fileIO.save(gameWithServer) shouldBe a[Success[?]]
      val loaded = fileIO.load().get
      val loadedServer = loaded.model.servers.head

      loadedServer.blockedUntilRound shouldBe None
    }
  }
}
