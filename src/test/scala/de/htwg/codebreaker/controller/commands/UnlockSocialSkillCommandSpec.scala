package de.htwg.codebreaker.controller.commands

import scala.util.{Success, Failure}

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.player.skill.SocialSkill
import de.htwg.codebreaker.controller.commands.player.UnlockSocialSkillCommand

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


class UnlockSocialSkillCommandSpec extends CommandTestBase {

  val testSkill = SocialSkill(
    id = "test_social_skill",
    name = "Test Social Skill",
    costXp = 50,
    successBonus = 10,
    description = "A test social skill"
  )

  "UnlockSocialSkillCommand" should {

    "unlock a social skill and reduce XP" in {
      val game = baseGame
      val player = game.model.players.head

      // Give player enough XP
      val playerWithXp = player.copy(availableXp = 100)
      val gameWithXp = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, playerWithXp)
        )
      )

      val cmd = UnlockSocialSkillCommand(
        playerIndex = 0,
        skill = testSkill
      )

      val result = cmd.doStep(gameWithXp).get

      val updated = result.model.players.head
      updated.availableXp shouldBe (100 - testSkill.costXp)
      updated.skills.unlockedSocialSkills should contain(testSkill.id)
    }

    "fail with invalid player index (negative)" in {
      val game = baseGame

      val cmd = UnlockSocialSkillCommand(
        playerIndex = -1,
        skill = testSkill
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "fail with invalid player index (too high)" in {
      val game = baseGame

      val cmd = UnlockSocialSkillCommand(
        playerIndex = 99,
        skill = testSkill
      )

      val result = cmd.doStep(game)
      result.isFailure shouldBe true
    }

    "fail if skill already unlocked" in {
      val game = baseGame
      val player = game.model.players.head

      // Give player XP and unlock the skill
      val playerWithSkill = player.copy(
        availableXp = 100,
        skills = player.skills.copy(
          unlockedSocialSkills = player.skills.unlockedSocialSkills + testSkill.id
        )
      )
      val gameWithSkill = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, playerWithSkill)
        )
      )

      val cmd = UnlockSocialSkillCommand(
        playerIndex = 0,
        skill = testSkill
      )

      val result = cmd.doStep(gameWithSkill)
      result.isFailure shouldBe true
    }

    "fail if not enough XP" in {
      val game = baseGame
      val player = game.model.players.head

      // Give player insufficient XP
      val playerWithLowXp = player.copy(availableXp = 10)
      val gameWithLowXp = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, playerWithLowXp)
        )
      )

      val cmd = UnlockSocialSkillCommand(
        playerIndex = 0,
        skill = testSkill
      )

      val result = cmd.doStep(gameWithLowXp)
      result.isFailure shouldBe true
    }

    "undo unlock skill command" in {
      val game = baseGame
      val player = game.model.players.head

      val playerWithXp = player.copy(availableXp = 100)
      val gameWithXp = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, playerWithXp)
        )
      )

      val initialSkills = playerWithXp.skills.unlockedSocialSkills.size

      val cmd = UnlockSocialSkillCommand(
        playerIndex = 0,
        skill = testSkill
      )

      val afterDo = cmd.doStep(gameWithXp).get
      val afterUndo = cmd.undoStep(afterDo).get

      val reverted = afterUndo.model.players.head
      reverted.availableXp shouldBe 100
      reverted.skills.unlockedSocialSkills should not contain testSkill.id
      reverted.skills.unlockedSocialSkills.size shouldBe initialSkills
    }

    "throw exception when undoing without previous state" in {
      val game = baseGame

      val cmd = UnlockSocialSkillCommand(
        playerIndex = 0,
        skill = testSkill
      )

      // Try to undo without calling doStep first
      val result = cmd.undoStep(game)
      result.isFailure shouldBe true
    }

    "correctly handle multiple different skills" in {
      val game = baseGame
      val player = game.model.players.head

      val skill1 = testSkill
      val skill2 = SocialSkill(
        id = "another_skill",
        name = "Another Skill",
        costXp = 30,
        successBonus = 5,
        description = "Another test skill"
      )

      // Give player lots of XP
      val playerWithXp = player.copy(availableXp = 200)
      val gameWithXp = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, playerWithXp)
        )
      )

      // Unlock first skill
      val cmd1 = UnlockSocialSkillCommand(0, skill1)
      val game1 = cmd1.doStep(gameWithXp).get

      // Unlock second skill
      val cmd2 = UnlockSocialSkillCommand(0, skill2)
      val game2 = cmd2.doStep(game1).get

      val finalPlayer = game2.model.players.head
      finalPlayer.skills.unlockedSocialSkills should contain(skill1.id)
      finalPlayer.skills.unlockedSocialSkills should contain(skill2.id)
      finalPlayer.availableXp shouldBe (200 - skill1.costXp - skill2.costXp)
    }

  }
}
