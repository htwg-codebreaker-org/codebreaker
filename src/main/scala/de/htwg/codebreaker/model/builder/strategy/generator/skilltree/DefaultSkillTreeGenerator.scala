package de.htwg.codebreaker.model.builder.strategy.generator.skilltree

import de.htwg.codebreaker.model.builder.strategy.SkillTreeGenerationStrategy
import de.htwg.codebreaker.model.player.skill.{HackSkill, SocialSkill}

object DefaultSkillTreeGenerator extends SkillTreeGenerationStrategy:
  
  override def generateSkills(): (List[HackSkill], List[SocialSkill]) = {
    val hackSkills = List(
        HackSkill(
        id = "script_kiddie",
        name = "Script Kiddie",
        costXp = 0,
        successBonus = 0,
        description = "Basis-Angriff, immer verfügbar"
      ),
      HackSkill(
        id = "password_cracking",
        name = "Password Cracking",
        costXp = 20,
        successBonus = 2,
        description = "Verbessere deine Fähigkeit, Passwörter zu knacken"
      ),
      HackSkill(
        id = "network_sniffing",
        name = "Network Sniffing",
        costXp = 30,
        successBonus = 3,
        description = "Erhöhe deine Erfolgschance beim Abhören von Netzwerken"
      ),
      HackSkill(
        id = "malware_deployment",
        name = "Malware Deployment",
        costXp = 40,
        successBonus = 4,
        description = "Steigere deine Fähigkeit, Malware zu installieren"
      ),
      HackSkill(
        id = "DDoS_attack",
        name = "DDoS Attack",
        costXp = 60,
        successBonus = 6,
        description = "Verbessere deine Fähigkeit, Dienste lahmzulegen"
      ),
      HackSkill(
        id = "bruteforce",
        name = "Brute Force",
        costXp = 50,
        successBonus = 5,
        description = "Erhöhe deine Erfolgschance bei Passwort-Angriffen"
      ),
      HackSkill(
        id = "phishing",
        name = "Phishing",
        costXp = 100,
        successBonus = 10,
        description = "Täusche Mitarbeiter, um Zugang zu erhalten"
      ),
      HackSkill(
        id = "sql_injection",
        name = "SQL Injection",
        costXp = 150,
        successBonus = 15,
        description = "Greife Datenbanken direkt an"
      )
      // ... weitere Hack Skills
    )

    val socialSkills = List(
      SocialSkill(
        id = "persuasion",
        name = "Überzeugungskraft",
        costXp = 50,
        successBonus = 5,
        description = "Erhöhe deine Erfolgschance bei Social Engineering"
      ),
      SocialSkill(
        id = "disguise",
        name = "Verkleidung",
        costXp = 100,
        successBonus = 10,
        description = "Tarnung für physische Infiltration"
      ),
      SocialSkill(
        id = "impersonation",
        name = "Imitation",
        costXp = 150,
        successBonus = 15,
        description = "Gebe dich als autorisierte Person aus"
      ),
      SocialSkill(
        id = "pretexting",
        name = "Vorwand",
        costXp = 200,
        successBonus = 20,
        description = "Erstelle glaubwürdige Vorwände für Informationen"
      ),
      SocialSkill(
        id = "tailgating",
        name = "Hinterherlaufen",
        costXp = 75,
        successBonus = 7,
        description = "Schleiche dich hinter autorisierte Personen"
      )
      // ... weitere Social Skills
    )

    (hackSkills, socialSkills)
  }