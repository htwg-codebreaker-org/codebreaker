// src/main/scala/de/htwg/codebreaker/model/Player.scala
package de.htwg.codebreaker.model

/**
 * Ein Spieler im Spiel.
 *
 * @param id                Eindeutige Spieler‑ID
 * @param name              Name des Spielers
 * @param tile              Position auf der Karte
 * @param cpu, ram, code    Aktuelle Ressourcen
 * @param level, xp         Fortschritt
 * @param cybersecurity     Sicherheits‑Wert
 * @param movementPoints    Verbleibende Bewegungspunkte (Standard: 5)
 * @param maxMovementPoints Maximale Bewegungspunkte pro Runde (Standard: 5)
 */
case class Player(
  id: Int,
  name: String,
  tile: Tile,
  cpu: Int,
  ram: Int,
  code: Int,
  level: Int,
  xp: Int,
  cybersecurity: Int,
  movementPoints: Int = 5,
  maxMovementPoints: Int = 5
)
