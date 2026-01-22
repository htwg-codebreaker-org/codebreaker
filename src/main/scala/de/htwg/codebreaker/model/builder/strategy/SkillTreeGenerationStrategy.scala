// In SkillTreeGenerationStrategy.scala
package de.htwg.codebreaker.model.builder.strategy

import de.htwg.codebreaker.model.player.skill.{HackSkill, SocialSkill}

trait SkillTreeGenerationStrategy:
  def generateSkills(): (List[HackSkill], List[SocialSkill])