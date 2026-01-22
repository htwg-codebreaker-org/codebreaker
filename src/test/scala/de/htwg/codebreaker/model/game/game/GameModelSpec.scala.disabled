package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class GameModelSpec extends AnyWordSpec with Matchers:

  val testTile = Tile(10, 10, Continent.Europe)
  val testPlayer = Player(0, "Test", testTile, 100, 100, 0, 0, 0, PlayerSkillTree(Set.empty), 30)
  val testServer = Server("TestServer", testTile, 50, 100, 200, false, ServerType.Private)
  val testSkill = HackSkill("test", "Test Skill", 100, 10, "Test description")
  val testMap = WorldMap.defaultMap

  "GameModel" should {

    "be created with all required fields" in {
      val model = GameModel(
        players = List(testPlayer),
        servers = List(testServer),
        worldMap = testMap,
        skills = List(testSkill)
      )

      model.players should have size 1
      model.servers should have size 1
      model.worldMap shouldBe testMap
      model.skills should have size 1
    }

    "support empty player list" in {
      val model = GameModel(
        players = Nil,
        servers = List(testServer),
        worldMap = testMap,
        skills = Nil
      )

      model.players shouldBe empty
    }

    "support empty server list" in {
      val model = GameModel(
        players = List(testPlayer),
        servers = Nil,
        worldMap = testMap,
        skills = Nil
      )

      model.servers shouldBe empty
    }

    "support empty skills list" in {
      val model = GameModel(
        players = List(testPlayer),
        servers = List(testServer),
        worldMap = testMap,
        skills = Nil
      )

      model.skills shouldBe empty
    }

    "support multiple players" in {
      val player2 = testPlayer.copy(id = 1, name = "Player2")
      val player3 = testPlayer.copy(id = 2, name = "Player3")
      
      val model = GameModel(
        players = List(testPlayer, player2, player3),
        servers = List(testServer),
        worldMap = testMap,
        skills = Nil
      )

      model.players should have size 3
    }

    "support multiple servers" in {
      val server2 = testServer.copy(name = "Server2")
      val server3 = testServer.copy(name = "Server3")
      
      val model = GameModel(
        players = List(testPlayer),
        servers = List(testServer, server2, server3),
        worldMap = testMap,
        skills = Nil
      )

      model.servers should have size 3
    }

    "support multiple skills" in {
      val skill2 = testSkill.copy(id = "skill2", name = "Skill 2")
      val skill3 = testSkill.copy(id = "skill3", name = "Skill 3")
      
      val model = GameModel(
        players = List(testPlayer),
        servers = List(testServer),
        worldMap = testMap,
        skills = List(testSkill, skill2, skill3)
      )

      model.skills should have size 3
    }

    "support updating players functionally" in {
      val original = GameModel(
        players = List(testPlayer),
        servers = List(testServer),
        worldMap = testMap,
        skills = Nil
      )

      val updatedPlayer = testPlayer.copy(cpu = 200)
      val updated = original.copy(players = List(updatedPlayer))

      original.players.head.cpu shouldBe 100
      updated.players.head.cpu shouldBe 200
    }

    "support updating servers functionally" in {
      val original = GameModel(
        players = List(testPlayer),
        servers = List(testServer),
        worldMap = testMap,
        skills = Nil
      )

      val updatedServer = testServer.copy(hacked = true)
      val updated = original.copy(servers = List(updatedServer))

      original.servers.head.hacked shouldBe false
      updated.servers.head.hacked shouldBe true
    }

    "support replacing world map" in {
      val original = GameModel(
        players = List(testPlayer),
        servers = List(testServer),
        worldMap = testMap,
        skills = Nil
      )

      val smallMap = WorldMap(10, 10, Vector(Tile(0, 0, Continent.Ocean)))
      val updated = original.copy(worldMap = smallMap)

      original.worldMap.width shouldBe 80
      updated.worldMap.width shouldBe 10
    }

    "support adding skills functionally" in {
      val original = GameModel(
        players = List(testPlayer),
        servers = List(testServer),
        worldMap = testMap,
        skills = List(testSkill)
      )

      val newSkill = testSkill.copy(id = "new", name = "New Skill")
      val updated = original.copy(skills = original.skills :+ newSkill)

      original.skills should have size 1
      updated.skills should have size 2
    }

    "support equality comparison" in {
      val model1 = GameModel(
        players = List(testPlayer),
        servers = List(testServer),
        worldMap = testMap,
        skills = List(testSkill)
      )
      
      val model2 = GameModel(
        players = List(testPlayer),
        servers = List(testServer),
        worldMap = testMap,
        skills = List(testSkill)
      )

      model1 shouldBe model2
    }

    "support accessing nested data" in {
      val model = GameModel(
        players = List(testPlayer),
        servers = List(testServer),
        worldMap = testMap,
        skills = List(testSkill)
      )

      model.players.head.name shouldBe "Test"
      model.servers.head.name shouldBe "TestServer"
      model.skills.head.name shouldBe "Test Skill"
      model.worldMap.width shouldBe 80
    }
  }
