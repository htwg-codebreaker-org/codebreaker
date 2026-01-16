package de.htwg.codebreaker.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PlayerSkillTreeSpec extends AnyWordSpec with Matchers:

  "PlayerSkillTree" should {

    "be created with empty skill set" in {
      val skillTree = PlayerSkillTree(Set.empty)
      
      skillTree.unlockedSkillIds shouldBe empty
    }

    "be created with unlocked skills" in {
      val skills = Set("bruteforce", "encryption", "firewall")
      val skillTree = PlayerSkillTree(skills)
      
      skillTree.unlockedSkillIds shouldBe skills
      skillTree.unlockedSkillIds should have size 3
    }

    "support adding skills functionally" in {
      val original = PlayerSkillTree(Set("skill1"))
      val updated = original.copy(unlockedSkillIds = original.unlockedSkillIds + "skill2")
      
      original.unlockedSkillIds should have size 1
      updated.unlockedSkillIds should have size 2
      updated.unlockedSkillIds should contain("skill1")
      updated.unlockedSkillIds should contain("skill2")
    }

    "support checking if skill is unlocked" in {
      val skillTree = PlayerSkillTree(Set("hack", "decrypt"))
      
      skillTree.unlockedSkillIds.contains("hack") shouldBe true
      skillTree.unlockedSkillIds.contains("decrypt") shouldBe true
      skillTree.unlockedSkillIds.contains("unknown") shouldBe false
    }

    "support removing skills functionally" in {
      val original = PlayerSkillTree(Set("skill1", "skill2", "skill3"))
      val updated = original.copy(unlockedSkillIds = original.unlockedSkillIds - "skill2")
      
      original.unlockedSkillIds should have size 3
      updated.unlockedSkillIds should have size 2
      updated.unlockedSkillIds should contain("skill1")
      updated.unlockedSkillIds should contain("skill3")
      updated.unlockedSkillIds should not contain "skill2"
    }

    "handle duplicate skill ids" in {
      val skills = Set("skill1", "skill1", "skill2")
      val skillTree = PlayerSkillTree(skills)
      
      // Set automatically handles duplicates
      skillTree.unlockedSkillIds should have size 2
    }

    "support equality comparison" in {
      val tree1 = PlayerSkillTree(Set("a", "b", "c"))
      val tree2 = PlayerSkillTree(Set("a", "b", "c"))
      val tree3 = PlayerSkillTree(Set("a", "b"))
      
      tree1 shouldBe tree2
      tree1 should not be tree3
    }
  }
