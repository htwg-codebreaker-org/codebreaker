// src/main/scala/de/htwg/codebreaker/model/SocialSkill.scala
package de.htwg.codebreaker.model.player.skill

/**
 * Definition eines Social-Skills (reine Daten).
 * Wird einmal im Spiel definiert.
 */
case class SocialSkill(
  id: String,              
  name: String,            // z.B. "bruteforce"
  costXp: Int,             // Kosten zum Freischalten
  successBonus: Int,       // Bonus in %
  description: String
)
