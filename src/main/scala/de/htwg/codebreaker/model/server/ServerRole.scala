// src/main/scala/de/htwg/codebreaker/model/server/ServerRole.scala
package de.htwg.codebreaker.model.server

enum ServerRoleType:
  case BitcoinMiner, DarknetHost, DataTrader, BotnetNode, Inactive

/**
 * Blueprint für eine Server-Role.
 * Definiert WELCHE Roles es gibt und ihre Eigenschaften.
 */
case class ServerRoleBlueprint(
  roleType: ServerRoleType,
  name: String,
  setupDurationRounds: Int,
  baseDetectionRisk: Int,
  availableActionIds: List[String],
  description: String,
  networkRange: Int,
)

/**
 * Blueprint für eine Role-Action.
 * Definiert WELCHE Actions es gibt und was sie tun.
 */
case class RoleActionBlueprint(
  id: String,
  name: String,
  roleType: ServerRoleType,
  durationRounds: Int,
  detectionRiskIncrease: Int,
  rewards: RoleActionReward,
  requirements: RoleActionRequirements,
  description: String
)

case class RoleActionReward(
  bitcoin: Int = 0,
  code: Int = 0,
  cpu: Int = 0,
  ram: Int = 0,

  // Zusätzliche Boni für den Laptop des Spielers
  networkRangeBonus: Int = 0,
  cybersecurityDamage: Int = 0
)


case class RoleActionRequirements(
  minCpu: Int = 0,
  minRam: Int = 0,
  minCode: Int = 0,
)