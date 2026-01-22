// src/main/scala/de/htwg/codebreaker/model/player/laptop/LaptopTool.scala
package de.htwg.codebreaker.model.player.laptop

/**
 * Definition eines Laptop-Tools (reine Daten).
 * Wird einmal im Spiel definiert.
 */
case class LaptopTool(
  id: String,               // z.B. "nmap"
  name: String,             // Anzeigename
  hackBonus: Int,           // Erfolgschance +X%
  stealthBonus: Int,        // Entdeckungsrisiko -X%
  speedBonus: Int,          // Dauer -X%
  description: String,
  availableActions: List[LaptopAction]
)