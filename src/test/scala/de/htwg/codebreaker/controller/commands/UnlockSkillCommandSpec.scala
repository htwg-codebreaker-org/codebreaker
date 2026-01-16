
package de.htwg.codebreaker.controller.commands

import scala.util.Success

import de.htwg.codebreaker.model.{PlayerSkillTree, HackSkill}
import de.htwg.codebreaker.model.game.Game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class UnlockSkillCommandSpec extends CommandTestBase {

  "UnlockSkillCommand" should {

    "unlock a skill for a player" in {
      val game = baseGame
      val player = game.model.players.head

      val lockedSkill = game.model.skills
        .find(s => !player.skills.unlockedSkillIds.contains(s.id))
        .get

      val richPlayer = player.copy(
        availableXp = lockedSkill.costXp + 10
      )

      val richGame = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, richPlayer)
        )
      )

      val cmd = UnlockSkillCommand(0, lockedSkill)
      val result = cmd.doStep(richGame).get

      result.model.players.head.skills.unlockedSkillIds should contain(lockedSkill.id)
    }

    "undo unlocking a skill" in {
      val game = baseGame
      val player = game.model.players.head

      val lockedSkill = game.model.skills
        .find(s => !player.skills.unlockedSkillIds.contains(s.id))
        .get

      val richPlayer = player.copy(availableXp = 100)

      val richGame = game.copy(
        model = game.model.copy(
          players = game.model.players.updated(0, richPlayer)
        )
      )

      val cmd = UnlockSkillCommand(0, lockedSkill)
      val afterDo = cmd.doStep(richGame).get
      val afterUndo = cmd.undoStep(afterDo).get

      afterUndo.model.players.head.skills.unlockedSkillIds should not contain lockedSkill.id
    }
  }
}
