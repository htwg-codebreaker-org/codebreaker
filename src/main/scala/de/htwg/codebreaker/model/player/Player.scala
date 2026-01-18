// src/main/scala/de/htwg/codebreaker/model/Player.scala
package de.htwg.codebreaker.model.player

import de.htwg.codebreaker.model.map.Tile
import de.htwg.codebreaker.model.player.laptop.Laptop
import de.htwg.codebreaker.model.player.skill.PlayerSkillTree

/**
 * Ein Spieler im Spiel.
 *
 * @param id                Eindeutige Spieler‑ID
 * @param name              Name des Spielers
 * @param tile              Position auf der Karte
 * @param level, xp         Fortschritt
 * @param cybersecurity     Sicherheits‑Wert
 * @param movementPoints    Verbleibende Bewegungspunkte
 * @param maxMovementPoints Maximale Bewegungspunkte pro Runde
 */
case class Player(
  id: Int,
  name: String,
  tile: Tile,
  laptop: Laptop,
  availableXp: Int,
  totalXpEarned: Int,
  skills: PlayerSkillTree,
  cybersecurity: Int,
  movementPoints: Int,
  maxMovementPoints: Int
)
