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
 * @param availableXp       Verfügbare Erfahrungspunkte
 * @param totalXpEarned     Gesamte verdiente Erfahrungspunkte
 * @param cybersecurity     Sicherheits‑Wert
 * @param movementPoints    Verbleibende Bewegungspunkte
 * @param maxMovementPoints Maximale Bewegungspunkte pro Runde
 * @param laptop            Laptop des Spielers
 * @param skills            Fähigkeitenbaum des Spielers
 * @param arrested          Ob der Spieler verhaftet ist
 */
case class Player(
  id: Int,
  name: String,
  tile: Tile,
  laptop: Laptop,
  availableXp: Int,
  totalXpEarned: Int,
  skills: PlayerSkillTree,
  arrested: Boolean,
  movementPoints: Int,
  maxMovementPoints: Int
)
