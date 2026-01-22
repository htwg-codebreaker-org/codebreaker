// src/main/scala/de/htwg/codebreaker/model/PlayerSkillTree.scala
package de.htwg.codebreaker.model.player.skill

case class PlayerSkillTree(
  unlockedHackSkills: Set[String] = Set.empty,
  unlockedSocialSkills: Set[String] = Set.empty
)



