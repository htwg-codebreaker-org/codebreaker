// src/main/scala/de/htwg/codebreaker/model/game/strategy/DefaultSkillTreeGenerator.scala
package de.htwg.codebreaker.model.game.strategy

import de.htwg.codebreaker.model.HackSkill

object DefaultSkillTreeGenerator extends SkillTreeGenerationStrategy {

  override def generateSkills(): List[HackSkill] = List(

    HackSkill(
      id = "script_kiddie",
      name = "Script Kiddie Attack",
      costXp = 0,
      successBonus = -25,
      description = "Kopierte Exploits ohne Verständnis – extrem hohe Entdeckungs- und Fehlschlagrate"
    ),

    HackSkill(
      id = "bruteforce",
      name = "Brute Force",
      costXp = 100,
      successBonus = 10,
      description = "Systematisches Passwort-Raten"
    ),

    HackSkill(
      id = "sql_injection",
      name = "SQL Injection",
      costXp = 300,
      successBonus = 25,
      description = "Datenbankzugriff durch Injection"
    ),

    HackSkill(
      id = "phishing",
      name = "Phishing",
      costXp = 350,
      successBonus = 30,
      description = "Täusche Mitarbeiter"
    ),

    HackSkill(
      id = "physical_access",
      name = "Physical Access",
      costXp = 600,
      successBonus = 40,
      description = "Vor-Ort Zugriff"
    )
  )
}
