// src/main/scala/de/htwg/codebreaker/model/server/Server.scala
package de.htwg.codebreaker.model.server

import de.htwg.codebreaker.model.map.Tile

/**
 * Typen von Servern im Spiel.
 */
enum ServerType:
  case Side, Firm, Cloud, Bank, Military, GKS, Private

/**
 * Ein Server auf der Weltkarte.
 *
 * @param name        Anzeigename
 * @param tile        Position auf der Karte
 * @param difficulty  Schwierigkeit (0–100)
 * @param rewardCpu   CPU‑Belohnung
 * @param rewardRam   RAM‑Belohnung
 * @param hacked      Wurde der Server bereits gehackt?
 * @param serverType  Art des Servers
 * @param hackedBy    Optional: Spieler‑ID, der den Server gehackt hat
 * @param claimedBy   Optional: Spieler‑ID, der den Server besitzt (wird beim Hack gesetzt)
 */
case class Server(
  name: String,
  tile: Tile,
  difficulty: Int,
  rewardCpu: Int,
  rewardRam: Int,
  hacked: Boolean,
  serverType: ServerType,
  hackedBy: Option[Int] = None,
  claimedBy: Option[Int] = None,
  blockedUntilRound: Option[Int] = None,
  installedRole: Option[InstalledServerRole] = None
)

/**
 * Blueprint für einen Fixed‑Server:
 * Name, feste Position und Werte‑Spannen für dynamische Erzeugung.
 */
case class ServerBlueprint(
  name: String,
  preferredPosition: (Int, Int),
  serverType: ServerType,
  difficultyRange: (Int, Int),
  rewardCpuRange: (Int, Int),
  rewardRamRange: (Int, Int)
)

case class InstalledServerRole(
  roleType: ServerRoleType,
  installStartRound: Int,
  isActive: Boolean = false,
  detectionRisk: Int = 0,
  runningActions: List[RunningRoleAction] = Nil
)

case class RunningRoleAction(
  actionId: String,
  startRound: Int,
  completionRound: Int,
  detectionIncrease: Int,
  expectedRewards: RoleActionReward
)

object Server:
  /** Weist einem Server eine Spieler‑ID als Besitzer zu (funktional). */
  def claim(server: Server, playerIndex: Int): Server =
    server.copy(claimedBy = Some(playerIndex))

  /** Entfernt eine Besitz‑Zuweisung von einem Server (funktional). */
  def unclaim(server: Server): Server =
    server.copy(claimedBy = None)
