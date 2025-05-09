// src/main/scala/de/htwg/codebreaker/model/Player.scala
package de.htwg.codebreaker.model

/**
 * Ein Spieler im Spiel.
 *
 * @param id             Eindeutige Spieler‑ID
 * @param name           Name des Spielers
 * @param tile           Position auf der Karte
 * @param cpu, ram, code Aktuelle Ressourcen
 * @param level, xp      Fortschritt
 * @param cybersecurity  Sicherheits‑Wert
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
  cybersecurity: Int
)
