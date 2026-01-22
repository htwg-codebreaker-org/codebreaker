package de.htwg.codebreaker.model.game.strategy

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class SkillTreeGenerationStrategySpec extends AnyWordSpec with Matchers:

  "SkillTreeGenerationStrategy trait" should {

    "define generateSkills method" in {
      val strategy = new SkillTreeGenerationStrategy {
        override def generateSkills(): List[HackSkill] = 
          List(HackSkill("test", "Test", 100, 10, "Test skill"))
      }

      val skills = strategy.generateSkills()
      skills should not be null
      skills shouldBe a[List[?]]
    }

    "support custom implementations" in {
      val customStrategy = new SkillTreeGenerationStrategy {
        override def generateSkills(): List[HackSkill] = 
          List(
            HackSkill("skill1", "Skill 1", 50, 5, "First skill"),
            HackSkill("skill2", "Skill 2", 100, 10, "Second skill"),
            HackSkill("skill3", "Skill 3", 150, 15, "Third skill")
          )
      }

      val skills = customStrategy.generateSkills()
      skills should have size 3
      skills.head.id shouldBe "skill1"
    }

    "support empty skill generation" in {
      val emptyStrategy = new SkillTreeGenerationStrategy {
        override def generateSkills(): List[HackSkill] = Nil
      }

      val skills = emptyStrategy.generateSkills()
      skills shouldBe empty
    }

    "allow different strategies to generate different skills" in {
      val strategy1 = new SkillTreeGenerationStrategy {
        override def generateSkills(): List[HackSkill] = 
          List(HackSkill("a", "A", 10, 1, "A"))
      }

      val strategy2 = new SkillTreeGenerationStrategy {
        override def generateSkills(): List[HackSkill] = 
          List(HackSkill("b", "B", 20, 2, "B"))
      }

      strategy1.generateSkills() should not be strategy2.generateSkills()
    }

    "support strategies that generate many skills" in {
      val largeStrategy = new SkillTreeGenerationStrategy {
        override def generateSkills(): List[HackSkill] = 
          (1 to 10).map(i => 
            HackSkill(s"skill$i", s"Skill $i", i * 10, i, s"Description $i")
          ).toList
      }

      val skills = largeStrategy.generateSkills()
      skills should have size 10
    }
  }
