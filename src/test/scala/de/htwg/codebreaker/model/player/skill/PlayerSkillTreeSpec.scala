package de.htwg.codebreaker.model.player.skill

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PlayerSkillTreeSpec extends AnyWordSpec with Matchers:

  "PlayerSkillTree" should {

    "be created with default empty skill sets" in {
      val skillTree = PlayerSkillTree()

      skillTree.unlockedHackSkills shouldBe Set.empty
      skillTree.unlockedSocialSkills shouldBe Set.empty
    }

    "be created with custom hack skills" in {
      val hackSkills = Set("bruteforce", "exploit", "ddos")
      val skillTree = PlayerSkillTree(unlockedHackSkills = hackSkills)

      skillTree.unlockedHackSkills shouldBe hackSkills
      skillTree.unlockedSocialSkills shouldBe Set.empty
    }

    "be created with custom social skills" in {
      val socialSkills = Set("phishing", "socialengineering", "pretexting")
      val skillTree = PlayerSkillTree(unlockedSocialSkills = socialSkills)

      skillTree.unlockedHackSkills shouldBe Set.empty
      skillTree.unlockedSocialSkills shouldBe socialSkills
    }

    "be created with both hack and social skills" in {
      val hackSkills = Set("bruteforce", "exploit")
      val socialSkills = Set("phishing", "socialengineering")
      val skillTree = PlayerSkillTree(hackSkills, socialSkills)

      skillTree.unlockedHackSkills shouldBe hackSkills
      skillTree.unlockedSocialSkills shouldBe socialSkills
    }

    "store single hack skill" in {
      val skillTree = PlayerSkillTree(unlockedHackSkills = Set("bruteforce"))

      skillTree.unlockedHackSkills should contain("bruteforce")
      skillTree.unlockedHackSkills.size shouldBe 1
    }

    "store single social skill" in {
      val skillTree = PlayerSkillTree(unlockedSocialSkills = Set("phishing"))

      skillTree.unlockedSocialSkills should contain("phishing")
      skillTree.unlockedSocialSkills.size shouldBe 1
    }

    "store multiple hack skills" in {
      val skills = Set("skill1", "skill2", "skill3", "skill4", "skill5")
      val skillTree = PlayerSkillTree(unlockedHackSkills = skills)

      skillTree.unlockedHackSkills shouldBe skills
      skillTree.unlockedHackSkills.size shouldBe 5
    }

    "store multiple social skills" in {
      val skills = Set("social1", "social2", "social3")
      val skillTree = PlayerSkillTree(unlockedSocialSkills = skills)

      skillTree.unlockedSocialSkills shouldBe skills
      skillTree.unlockedSocialSkills.size shouldBe 3
    }

    "support equality comparison" in {
      val tree1 = PlayerSkillTree(Set("hack1"), Set("social1"))
      val tree2 = PlayerSkillTree(Set("hack1"), Set("social1"))
      val tree3 = PlayerSkillTree(Set("hack2"), Set("social1"))

      tree1 shouldBe tree2
      tree1 should not be tree3
    }

    "support copy with added hack skill" in {
      val original = PlayerSkillTree(Set("skill1"))
      val updated = original.copy(unlockedHackSkills = original.unlockedHackSkills + "skill2")

      updated.unlockedHackSkills should contain("skill1")
      updated.unlockedHackSkills should contain("skill2")
      updated.unlockedHackSkills.size shouldBe 2
      original.unlockedHackSkills.size shouldBe 1
    }

    "support copy with added social skill" in {
      val original = PlayerSkillTree(unlockedSocialSkills = Set("social1"))
      val updated = original.copy(unlockedSocialSkills = original.unlockedSocialSkills + "social2")

      updated.unlockedSocialSkills should contain("social1")
      updated.unlockedSocialSkills should contain("social2")
      updated.unlockedSocialSkills.size shouldBe 2
      original.unlockedSocialSkills.size shouldBe 1
    }

    "support copy with removed hack skill" in {
      val original = PlayerSkillTree(Set("skill1", "skill2", "skill3"))
      val updated = original.copy(unlockedHackSkills = original.unlockedHackSkills - "skill2")

      updated.unlockedHackSkills should not contain "skill2"
      updated.unlockedHackSkills should contain("skill1")
      updated.unlockedHackSkills should contain("skill3")
      updated.unlockedHackSkills.size shouldBe 2
      original.unlockedHackSkills.size shouldBe 3
    }

    "support copy with removed social skill" in {
      val original = PlayerSkillTree(unlockedSocialSkills = Set("social1", "social2"))
      val updated = original.copy(unlockedSocialSkills = original.unlockedSocialSkills - "social1")

      updated.unlockedSocialSkills should not contain "social1"
      updated.unlockedSocialSkills should contain("social2")
      updated.unlockedSocialSkills.size shouldBe 1
      original.unlockedSocialSkills.size shouldBe 2
    }

    "support clearing all hack skills via copy" in {
      val original = PlayerSkillTree(Set("skill1", "skill2"), Set("social1"))
      val cleared = original.copy(unlockedHackSkills = Set.empty)

      cleared.unlockedHackSkills shouldBe Set.empty
      cleared.unlockedSocialSkills should contain("social1")
      original.unlockedHackSkills.size shouldBe 2
    }

    "support clearing all social skills via copy" in {
      val original = PlayerSkillTree(Set("hack1"), Set("social1", "social2"))
      val cleared = original.copy(unlockedSocialSkills = Set.empty)

      cleared.unlockedSocialSkills shouldBe Set.empty
      cleared.unlockedHackSkills should contain("hack1")
      original.unlockedSocialSkills.size shouldBe 2
    }

    "maintain immutability when copying" in {
      val original = PlayerSkillTree(Set("hack1"), Set("social1"))
      val modified = original.copy(
        unlockedHackSkills = Set("hack2"),
        unlockedSocialSkills = Set("social2")
      )

      original.unlockedHackSkills should contain("hack1")
      original.unlockedSocialSkills should contain("social1")
      modified.unlockedHackSkills should contain("hack2")
      modified.unlockedSocialSkills should contain("social2")
    }

    "handle skill IDs with special characters" in {
      val skillTree = PlayerSkillTree(
        Set("skill_with_underscore", "skill-with-dash", "skill.with.dot"),
        Set("social_skill_1", "social-skill-2")
      )

      skillTree.unlockedHackSkills should contain("skill_with_underscore")
      skillTree.unlockedHackSkills should contain("skill-with-dash")
      skillTree.unlockedHackSkills should contain("skill.with.dot")
      skillTree.unlockedSocialSkills should contain("social_skill_1")
      skillTree.unlockedSocialSkills should contain("social-skill-2")
    }

    "handle empty strings as skill IDs" in {
      val skillTree = PlayerSkillTree(Set(""), Set(""))

      skillTree.unlockedHackSkills should contain("")
      skillTree.unlockedSocialSkills should contain("")
    }

    "handle long skill ID names" in {
      val longSkillId = "a" * 1000
      val skillTree = PlayerSkillTree(Set(longSkillId))

      skillTree.unlockedHackSkills should contain(longSkillId)
    }

    "handle many skills" in {
      val manyHackSkills = (1 to 100).map(i => s"hack_skill_$i").toSet
      val manySocialSkills = (1 to 100).map(i => s"social_skill_$i").toSet
      val skillTree = PlayerSkillTree(manyHackSkills, manySocialSkills)

      skillTree.unlockedHackSkills.size shouldBe 100
      skillTree.unlockedSocialSkills.size shouldBe 100
      skillTree.unlockedHackSkills should contain("hack_skill_50")
      skillTree.unlockedSocialSkills should contain("social_skill_75")
    }

    "support checking if hack skill is unlocked" in {
      val skillTree = PlayerSkillTree(Set("bruteforce", "exploit"))

      skillTree.unlockedHackSkills.contains("bruteforce") shouldBe true
      skillTree.unlockedHackSkills.contains("exploit") shouldBe true
      skillTree.unlockedHackSkills.contains("ddos") shouldBe false
    }

    "support checking if social skill is unlocked" in {
      val skillTree = PlayerSkillTree(unlockedSocialSkills = Set("phishing", "pretexting"))

      skillTree.unlockedSocialSkills.contains("phishing") shouldBe true
      skillTree.unlockedSocialSkills.contains("pretexting") shouldBe true
      skillTree.unlockedSocialSkills.contains("socialengineering") shouldBe false
    }

    "distinguish between hack and social skills" in {
      val skillTree = PlayerSkillTree(Set("hackskill"), Set("socialskill"))

      skillTree.unlockedHackSkills.contains("hackskill") shouldBe true
      skillTree.unlockedSocialSkills.contains("socialskill") shouldBe true
      skillTree.unlockedHackSkills.contains("socialskill") shouldBe false
      skillTree.unlockedSocialSkills.contains("hackskill") shouldBe false
    }

    "allow same skill ID in both hack and social skills" in {
      val skillTree = PlayerSkillTree(Set("universal"), Set("universal"))

      skillTree.unlockedHackSkills should contain("universal")
      skillTree.unlockedSocialSkills should contain("universal")
    }

    "support case-sensitive skill IDs" in {
      val skillTree = PlayerSkillTree(Set("BruteForce", "bruteforce", "BRUTEFORCE"))

      skillTree.unlockedHackSkills.size shouldBe 3
      skillTree.unlockedHackSkills should contain("BruteForce")
      skillTree.unlockedHackSkills should contain("bruteforce")
      skillTree.unlockedHackSkills should contain("BRUTEFORCE")
    }

    "maintain set properties (no duplicates)" in {
      // Sets automatically handle duplicates, but let's verify behavior
      val skillsWithDuplicates = Set("skill1", "skill2", "skill1", "skill3")
      val skillTree = PlayerSkillTree(unlockedHackSkills = skillsWithDuplicates)

      skillTree.unlockedHackSkills.size shouldBe 3
      skillTree.unlockedHackSkills should contain("skill1")
      skillTree.unlockedHackSkills should contain("skill2")
      skillTree.unlockedHackSkills should contain("skill3")
    }
  }

  "PlayerSkillTree integration" should {

    "support progressive skill unlocking" in {
      var skillTree = PlayerSkillTree()

      skillTree = skillTree.copy(unlockedHackSkills = skillTree.unlockedHackSkills + "skill1")
      skillTree.unlockedHackSkills.size shouldBe 1

      skillTree = skillTree.copy(unlockedHackSkills = skillTree.unlockedHackSkills + "skill2")
      skillTree.unlockedHackSkills.size shouldBe 2

      skillTree = skillTree.copy(unlockedSocialSkills = skillTree.unlockedSocialSkills + "social1")
      skillTree.unlockedSocialSkills.size shouldBe 1

      skillTree.unlockedHackSkills should contain allOf ("skill1", "skill2")
      skillTree.unlockedSocialSkills should contain("social1")
    }

    "support replacing entire skill sets" in {
      val original = PlayerSkillTree(Set("old1", "old2"), Set("oldsocial1"))
      val replaced = original.copy(
        unlockedHackSkills = Set("new1", "new2", "new3"),
        unlockedSocialSkills = Set("newsocial1", "newsocial2")
      )

      replaced.unlockedHackSkills should not contain "old1"
      replaced.unlockedHackSkills should not contain "old2"
      replaced.unlockedHackSkills should contain allOf ("new1", "new2", "new3")
      replaced.unlockedSocialSkills should not contain "oldsocial1"
      replaced.unlockedSocialSkills should contain allOf ("newsocial1", "newsocial2")
    }

    "support merging skill trees" in {
      val tree1 = PlayerSkillTree(Set("hack1", "hack2"), Set("social1"))
      val tree2 = PlayerSkillTree(Set("hack3", "hack4"), Set("social2", "social3"))

      val merged = PlayerSkillTree(
        tree1.unlockedHackSkills ++ tree2.unlockedHackSkills,
        tree1.unlockedSocialSkills ++ tree2.unlockedSocialSkills
      )

      merged.unlockedHackSkills.size shouldBe 4
      merged.unlockedSocialSkills.size shouldBe 3
      merged.unlockedHackSkills should contain allOf ("hack1", "hack2", "hack3", "hack4")
      merged.unlockedSocialSkills should contain allOf ("social1", "social2", "social3")
    }

    "work with player progression scenario" in {
      // Start with no skills
      var skillTree = PlayerSkillTree()

      // Unlock starter skill
      skillTree = skillTree.copy(unlockedHackSkills = Set("script_kiddie"))
      skillTree.unlockedHackSkills should contain("script_kiddie")

      // Unlock more hack skills
      skillTree = skillTree.copy(
        unlockedHackSkills = skillTree.unlockedHackSkills ++ Set("bruteforce", "portscan")
      )
      skillTree.unlockedHackSkills.size shouldBe 3

      // Unlock social skills
      skillTree = skillTree.copy(
        unlockedSocialSkills = Set("phishing", "pretexting")
      )
      skillTree.unlockedSocialSkills.size shouldBe 2

      // Final verification
      skillTree.unlockedHackSkills should contain allOf ("script_kiddie", "bruteforce", "portscan")
      skillTree.unlockedSocialSkills should contain allOf ("phishing", "pretexting")
    }
  }
