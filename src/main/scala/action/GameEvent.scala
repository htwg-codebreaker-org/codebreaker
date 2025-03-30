// src/main/scala/action/GameEvent.scala
package action

import model.Player

case class GameEvent(
  name: String,
  description: String,
  applyEffect: Player => Player
)

object GameEvents:
  val Rückverfolgung = GameEvent(
    "Rückverfolgung",
    "Deine IP wurde zurückverfolgt. CPU -15",
    p => p.copy(cpu = (p.cpu - 15).max(0))
  )

  val Datenleck = GameEvent(
    "Datenleck",
    "Geheime Daten gefunden: +20 Codezeilen",
    p => p.copy(code = p.code + 20)
  )

  val Honeypot = GameEvent(
    "Honeypot-Falle",
    "Ein Köderserver! CPU -30, RAM -10",
    p => p.copy(cpu = (p.cpu - 30).max(0), ram = (p.ram - 10).max(0))
  )

  val SocialBoost = GameEvent(
    "Social Engineering Erfolg",
    "+15% Erfolg auf nächsten Hack",
    p => p.copy(cybersecurity = (p.cybersecurity + 15).min(100))
  )

  val Lockdown = GameEvent(
    "System Lockdown",
    "GKS hat sich isoliert. -50% Erfolg für 3 Runden",
    p => p.copy(cybersecurity = (p.cybersecurity - 50).max(0)) // Placeholder Effekt
  )

  val NotEnoughMoney = GameEvent(
    "Nicht genug Geld",
    "Du hast nicht genug Geld für diese Aktion.",
    p => p // Keine Änderung am Spieler
  )
