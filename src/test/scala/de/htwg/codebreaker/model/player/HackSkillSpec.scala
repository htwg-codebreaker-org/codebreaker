package de.htwg.codebreaker.model.player

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model.player.skill.HackSkill

class HackSkillSpec extends AnyWordSpec with Matchers:

  "HackSkill" should {

    "be created with all required fields" in {
      val skill = HackSkill(
        id = "bruteforce",
        name = "Brute Force",
        costXp = 100,
        successBonus = 15,
        description = "Increases hack success rate"
      )

      skill.id shouldBe "bruteforce"
      skill.name shouldBe "Brute Force"
      skill.costXp shouldBe 100
      skill.successBonus shouldBe 15
      skill.description shouldBe "Increases hack success rate"
    }

    "support different cost values" in {
      val cheapSkill = HackSkill("cheap", "Cheap", 50, 5, "Low cost skill")
      val expensiveSkill = HackSkill("expensive", "Expensive", 500, 50, "High cost skill")
      
      cheapSkill.costXp shouldBe 50
      expensiveSkill.costXp shouldBe 500
    }

    "support different success bonus values" in {
      val lowBonus = HackSkill("low", "Low Bonus", 100, 5, "Small bonus")
      val highBonus = HackSkill("high", "High Bonus", 200, 50, "Large bonus")
      
      lowBonus.successBonus shouldBe 5
      highBonus.successBonus shouldBe 50
    }

    "support equality comparison" in {
      val skill1 = HackSkill("test", "Test", 100, 10, "Test skill")
      val skill2 = HackSkill("test", "Test", 100, 10, "Test skill")
      val skill3 = HackSkill("other", "Other", 100, 10, "Other skill")
      
      skill1 shouldBe skill2
      skill1 should not be skill3
    }

    "support copy with modifications" in {
      val original = HackSkill("original", "Original", 100, 10, "Original skill")
      val modified = original.copy(costXp = 150, successBonus = 20)
      
      modified.id shouldBe "original"
      modified.name shouldBe "Original"
      modified.costXp shouldBe 150
      modified.successBonus shouldBe 20
      modified.description shouldBe "Original skill"
    }

    "create skills with unique ids" in {
      val skill1 = HackSkill("skill1", "Skill One", 100, 10, "First")
      val skill2 = HackSkill("skill2", "Skill Two", 100, 10, "Second")
      
      skill1.id should not be skill2.id
    }

    "allow same name for different skills" in {
      val skill1 = HackSkill("id1", "Same Name", 100, 10, "First")
      val skill2 = HackSkill("id2", "Same Name", 100, 10, "Second")
      
      skill1.name shouldBe skill2.name
      skill1 should not be skill2
    }

    "support zero cost skills" in {
      val freeSkill = HackSkill("free", "Free", 0, 10, "No cost")
      freeSkill.costXp shouldBe 0
    }

    "support zero bonus skills" in {
      val noBonus = HackSkill("nobonus", "No Bonus", 100, 0, "No bonus")
      noBonus.successBonus shouldBe 0
    }

    "handle empty description" in {
      val skill = HackSkill("test", "Test", 100, 10, "")
      skill.description shouldBe ""
    }

    "handle long description" in {
      val longDesc = "This is a very long description " * 10
      val skill = HackSkill("long", "Long", 100, 10, longDesc)
      skill.description shouldBe longDesc
    }
  }
