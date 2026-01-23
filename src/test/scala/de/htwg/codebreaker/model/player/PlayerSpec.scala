package de.htwg.codebreaker.model.player

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model.map.{Tile, Continent}
import de.htwg.codebreaker.model.player.laptop.{Laptop, LaptopHardware, LaptopInstalledTools}
import de.htwg.codebreaker.model.player.skill.PlayerSkillTree

class PlayerSpec extends AnyWordSpec with Matchers:

  "Player" should {

    val testTile = Tile(5, 10, Continent.Europe)
    val testLaptop = Laptop(
      LaptopHardware(100, 200, 50, 4, 10),
      LaptopInstalledTools.empty,
      List.empty,
      None,
      75
    )
    val testSkills = PlayerSkillTree()

    "be created with all required fields" in {
      val player = Player(
        id = 1,
        name = "Alice",
        tile = testTile,
        laptop = testLaptop,
        availableXp = 100,
        totalXpEarned = 500,
        skills = testSkills,
        arrested = false,
        movementPoints = 3,
        maxMovementPoints = 5
      )

      player.id shouldBe 1
      player.name shouldBe "Alice"
      player.tile shouldBe testTile
      player.laptop shouldBe testLaptop
      player.availableXp shouldBe 100
      player.totalXpEarned shouldBe 500
      player.skills shouldBe testSkills
      player.arrested shouldBe false
      player.movementPoints shouldBe 3
      player.maxMovementPoints shouldBe 5
    }

    "support different player IDs" in {
      val player1 = Player(1, "Player1", testTile, testLaptop, 0, 0, testSkills, false, 3, 5)
      val player2 = Player(2, "Player2", testTile, testLaptop, 0, 0, testSkills, false, 3, 5)

      player1.id shouldBe 1
      player2.id shouldBe 2
      player1 should not be player2
    }

    "support different player names" in {
      val player = Player(1, "HackerX", testTile, testLaptop, 0, 0, testSkills, false, 3, 5)

      player.name shouldBe "HackerX"
    }

    "support different tile positions" in {
      val tile1 = Tile(0, 0, Continent.NorthAmerica)
      val tile2 = Tile(15, 20, Continent.Asia)

      val player1 = Player(1, "Player1", tile1, testLaptop, 0, 0, testSkills, false, 3, 5)
      val player2 = Player(2, "Player2", tile2, testLaptop, 0, 0, testSkills, false, 3, 5)

      player1.tile.x shouldBe 0
      player1.tile.y shouldBe 0
      player1.tile.continent shouldBe Continent.NorthAmerica

      player2.tile.x shouldBe 15
      player2.tile.y shouldBe 20
      player2.tile.continent shouldBe Continent.Asia
    }

    "support XP management" in {
      val player = Player(1, "Player", testTile, testLaptop, 150, 500, testSkills, false, 3, 5)

      player.availableXp shouldBe 150
      player.totalXpEarned shouldBe 500
    }

    "support gaining XP through copy" in {
      val player = Player(1, "Player", testTile, testLaptop, 100, 500, testSkills, false, 3, 5)
      val afterGain = player.copy(availableXp = 200, totalXpEarned = 600)

      afterGain.availableXp shouldBe 200
      afterGain.totalXpEarned shouldBe 600
      player.availableXp shouldBe 100
      player.totalXpEarned shouldBe 500
    }

    "support spending XP through copy" in {
      val player = Player(1, "Player", testTile, testLaptop, 100, 500, testSkills, false, 3, 5)
      val afterSpend = player.copy(availableXp = 50)

      afterSpend.availableXp shouldBe 50
      afterSpend.totalXpEarned shouldBe 500
      player.availableXp shouldBe 100
    }

    "support arrested status" in {
      val freePlayer = Player(1, "Free", testTile, testLaptop, 0, 0, testSkills, false, 3, 5)
      val arrestedPlayer = Player(2, "Arrested", testTile, testLaptop, 0, 0, testSkills, true, 0, 5)

      freePlayer.arrested shouldBe false
      arrestedPlayer.arrested shouldBe true
    }

    "support changing arrested status through copy" in {
      val player = Player(1, "Player", testTile, testLaptop, 0, 0, testSkills, false, 3, 5)
      val arrested = player.copy(arrested = true, movementPoints = 0)

      arrested.arrested shouldBe true
      arrested.movementPoints shouldBe 0
      player.arrested shouldBe false
    }

    "support movement points" in {
      val player = Player(1, "Player", testTile, testLaptop, 0, 0, testSkills, false, 3, 5)

      player.movementPoints shouldBe 3
      player.maxMovementPoints shouldBe 5
    }

    "support movement through copy" in {
      val player = Player(1, "Player", testTile, testLaptop, 0, 0, testSkills, false, 5, 5)
      val newTile = Tile(6, 10, Continent.Europe)
      val afterMove = player.copy(tile = newTile, movementPoints = 4)

      afterMove.tile shouldBe newTile
      afterMove.movementPoints shouldBe 4
      player.movementPoints shouldBe 5
    }

    "support resetting movement points through copy" in {
      val player = Player(1, "Player", testTile, testLaptop, 0, 0, testSkills, false, 2, 5)
      val afterReset = player.copy(movementPoints = player.maxMovementPoints)

      afterReset.movementPoints shouldBe 5
      player.movementPoints shouldBe 2
    }

    "support skill tree" in {
      val skills = PlayerSkillTree(
        unlockedHackSkills = Set("bruteforce", "exploit"),
        unlockedSocialSkills = Set("phishing")
      )
      val player = Player(1, "Player", testTile, testLaptop, 0, 0, skills, false, 3, 5)

      player.skills.unlockedHackSkills shouldBe Set("bruteforce", "exploit")
      player.skills.unlockedSocialSkills shouldBe Set("phishing")
    }

    "support unlocking skills through copy" in {
      val player = Player(1, "Player", testTile, testLaptop, 100, 500, testSkills, false, 3, 5)
      val newSkills = player.skills.copy(unlockedHackSkills = Set("bruteforce"))
      val afterUnlock = player.copy(skills = newSkills, availableXp = 50)

      afterUnlock.skills.unlockedHackSkills shouldBe Set("bruteforce")
      afterUnlock.availableXp shouldBe 50
      player.skills.unlockedHackSkills shouldBe Set.empty
    }

    "support laptop upgrades through copy" in {
      val player = Player(1, "Player", testTile, testLaptop, 0, 0, testSkills, false, 3, 5)
      val upgradedHardware = player.laptop.hardware.copy(cpu = 200, ram = 400)
      val upgradedLaptop = player.laptop.copy(hardware = upgradedHardware)
      val afterUpgrade = player.copy(laptop = upgradedLaptop)

      afterUpgrade.laptop.hardware.cpu shouldBe 200
      afterUpgrade.laptop.hardware.ram shouldBe 400
      player.laptop.hardware.cpu shouldBe 100
      player.laptop.hardware.ram shouldBe 200
    }

    "support equality comparison" in {
      val player1 = Player(1, "Player", testTile, testLaptop, 100, 500, testSkills, false, 3, 5)
      val player2 = Player(1, "Player", testTile, testLaptop, 100, 500, testSkills, false, 3, 5)
      val player3 = Player(2, "Other", testTile, testLaptop, 100, 500, testSkills, false, 3, 5)

      player1 shouldBe player2
      player1 should not be player3
    }

    "support zero movement points" in {
      val player = Player(1, "Player", testTile, testLaptop, 0, 0, testSkills, false, 0, 5)

      player.movementPoints shouldBe 0
    }

    "support zero XP" in {
      val player = Player(1, "Player", testTile, testLaptop, 0, 0, testSkills, false, 3, 5)

      player.availableXp shouldBe 0
      player.totalXpEarned shouldBe 0
    }

    "support different max movement points" in {
      val slowPlayer = Player(1, "Slow", testTile, testLaptop, 0, 0, testSkills, false, 3, 3)
      val fastPlayer = Player(2, "Fast", testTile, testLaptop, 0, 0, testSkills, false, 10, 10)

      slowPlayer.maxMovementPoints shouldBe 3
      fastPlayer.maxMovementPoints shouldBe 10
    }

    "maintain immutability when copying" in {
      val original = Player(1, "Original", testTile, testLaptop, 100, 500, testSkills, false, 3, 5)
      val modified = original.copy(name = "Modified", availableXp = 50)

      original.name shouldBe "Original"
      original.availableXp shouldBe 100
      modified.name shouldBe "Modified"
      modified.availableXp shouldBe 50
    }
  }
