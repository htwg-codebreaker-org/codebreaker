// src/main/scala/de/htwg/codebreaker/model/game/strategy/SkillTreeGenerationStrategy.scala
package de.htwg.codebreaker.model.game.strategy

import de.htwg.codebreaker.model.HackSkill

trait SkillTreeGenerationStrategy {
  def generateSkills(): List[HackSkill]
}
