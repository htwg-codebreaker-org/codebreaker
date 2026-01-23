package de.htwg.codebreaker.model.builder.strategy.generator.player

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model.map.{WorldMap, Tile, Continent}

class UnlockAllPlayerGeneratorSpec extends AnyWordSpec with Matchers:

  "UnlockAllPlayerGenerator" should {

    "generate correct number of players" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(3, map)

      players.length shouldBe 3
    }

    "generate players with sequential IDs starting from 1" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(4, map)

      players.map(_.id) shouldBe List(1, 2, 3, 4)
    }

    "generate players with default names" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(3, map)

      players.head.name shouldBe "Spieler 1"
      players(1).name shouldBe "Spieler 2"
      players(2).name shouldBe "Spieler 3"
    }

    "place players on land tiles only" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(5, map)

      players.foreach { player =>
        player.tile.continent.isLand shouldBe true
      }
    }

    "avoid specified tiles when placing players" in {
      val map = WorldMap.defaultMap
      val avoidTile = map.tiles.find(_.continent.isLand).get
      val players = UnlockAllPlayerGenerator.generatePlayers(3, map, List(avoidTile))

      players.foreach { player =>
        player.tile should not be avoidTile
      }
    }

    "generate players with boosted laptop hardware" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(2, map)

      players.foreach { player =>
        player.laptop.hardware.cpu shouldBe 1000
        player.laptop.hardware.ram shouldBe 1000
        player.laptop.hardware.code shouldBe 1000
        player.laptop.hardware.kerne shouldBe 1
        player.laptop.hardware.networkRange shouldBe 1000
      }
    }

    "generate players with very high cybersecurity level" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(2, map)

      players.foreach { player =>
        player.laptop.cybersecurity shouldBe 1000
      }
    }

    "generate players with starter tools" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(2, map)

      players.foreach { player =>
        val toolIds = player.laptop.tools.installedTools.map(_.id).toSet
        toolIds should contain("nmap")
        toolIds should contain("wireshark")
      }
    }

    "generate players with initial skill set" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(2, map)

      players.foreach { player =>
        player.skills.unlockedHackSkills should contain("script_kiddie")
      }
    }

    "generate players with boosted XP" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(2, map)

      players.foreach { player =>
        player.availableXp shouldBe 1000
        player.totalXpEarned shouldBe 10001000
      }
    }

    "generate players with boosted movement points" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(2, map)

      players.foreach { player =>
        player.movementPoints shouldBe 1000
        player.maxMovementPoints shouldBe 1000
      }
    }

    "generate players who are not arrested" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(2, map)

      players.foreach { player =>
        player.arrested shouldBe false
      }
    }

    "generate players with no running laptop actions" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(2, map)

      players.foreach { player =>
        player.laptop.runningActions shouldBe empty
      }
    }

    "generate players with no running internet search" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(2, map)

      players.foreach { player =>
        player.laptop.runningInternetSearch shouldBe None
      }
    }

    "generate single player when count is 1" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(1, map)

      players.length shouldBe 1
      players.head.id shouldBe 1
      players.head.name shouldBe "Spieler 1"
    }

    "place players on different tiles" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(5, map)

      val tiles = players.map(_.tile)
      tiles.distinct.length shouldBe tiles.length
    }

    "generate players with valid tiles from the map" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(3, map)

      players.foreach { player =>
        map.tiles should contain(player.tile)
      }
    }

    "handle generation with multiple avoided tiles" in {
      val map = WorldMap.defaultMap
      val landTiles = map.tiles.filter(_.continent.isLand)
      val avoidTiles = landTiles.take(5).toList
      val players = UnlockAllPlayerGenerator.generatePlayers(3, map, avoidTiles)

      players.foreach { player =>
        avoidTiles should not contain player.tile
      }
    }

    "generate players with correct laptop tool structure" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(1, map)

      val player = players.head
      player.laptop.tools.installedTools should not be empty
      player.laptop.tools.installedTools.foreach { tool =>
        tool.id should not be empty
        tool.name should not be empty
      }
    }

    "generate multiple players with consistent initial state" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(4, map)

      val first = players.head
      players.tail.foreach { player =>
        player.laptop.hardware.cpu shouldBe first.laptop.hardware.cpu
        player.laptop.hardware.ram shouldBe first.laptop.hardware.ram
        player.laptop.hardware.code shouldBe first.laptop.hardware.code
        player.laptop.cybersecurity shouldBe first.laptop.cybersecurity
        player.availableXp shouldBe first.availableXp
        player.totalXpEarned shouldBe first.totalXpEarned
        player.movementPoints shouldBe first.movementPoints
        player.maxMovementPoints shouldBe first.maxMovementPoints
        player.arrested shouldBe first.arrested
      }
    }

    "generate players with significantly higher stats than DefaultPlayerGenerator" in {
      val map = WorldMap.defaultMap
      val defaultPlayers = DefaultPlayerGenerator.generatePlayers(1, map)
      val unlockAllPlayers = UnlockAllPlayerGenerator.generatePlayers(1, map)

      val defaultPlayer = defaultPlayers.head
      val unlockAllPlayer = unlockAllPlayers.head

      unlockAllPlayer.laptop.hardware.cpu should be > defaultPlayer.laptop.hardware.cpu
      unlockAllPlayer.laptop.hardware.ram should be > defaultPlayer.laptop.hardware.ram
      unlockAllPlayer.laptop.hardware.networkRange should be > defaultPlayer.laptop.hardware.networkRange
      unlockAllPlayer.laptop.cybersecurity should be > defaultPlayer.laptop.cybersecurity
      unlockAllPlayer.availableXp should be > defaultPlayer.availableXp
      unlockAllPlayer.totalXpEarned should be > defaultPlayer.totalXpEarned
      unlockAllPlayer.movementPoints should be > defaultPlayer.movementPoints
      unlockAllPlayer.maxMovementPoints should be > defaultPlayer.maxMovementPoints
    }

    "have hardware values of exactly 1000" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(1, map)
      val player = players.head

      player.laptop.hardware.cpu shouldBe 1000
      player.laptop.hardware.ram shouldBe 1000
      player.laptop.hardware.code shouldBe 1000
      player.laptop.hardware.networkRange shouldBe 1000
      player.laptop.cybersecurity shouldBe 1000
    }

    "have XP and movement values appropriate for testing" in {
      val map = WorldMap.defaultMap
      val players = UnlockAllPlayerGenerator.generatePlayers(1, map)
      val player = players.head

      player.availableXp shouldBe 1000
      player.totalXpEarned shouldBe 10001000
      player.movementPoints shouldBe 1000
      player.maxMovementPoints shouldBe 1000
    }
  }
