// src/main/scala/de/htwg/codebreaker/model/HackSkill.scala
package de.htwg.codebreaker.model.player.skill

/**
 * Definition eines Hack-Skills (reine Daten).
 * Wird einmal im Spiel definiert.
 */
case class HackSkill(
  id: String,              
  name: String,            // z.B. "bruteforce"
  costXp: Int,             // Kosten zum Freischalten
  successBonus: Int,       // Bonus in %
  description: String
)
