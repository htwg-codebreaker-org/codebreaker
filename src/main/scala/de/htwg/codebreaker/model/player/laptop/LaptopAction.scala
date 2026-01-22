package de.htwg.codebreaker.model.player.laptop

import com.fasterxml.jackson.databind.JsonSerializable.Base

case class RunningLaptopAction(
  action: LaptopAction,
  startRound: Int,
  completionRound: Int,
  targetServer: String,
)

case class LaptopAction(
  id: String,              // z.B. "nmap_quick_scan"
  name: String,            // "Quick Scan"
  actionType: LaptopActionType,
  durationRounds: Int,     // Basis-Dauer (kann durch Tool reduziert werden)
  coreCost: Int,
  cpuCost: Int,
  ramCost: Int,
  description: String,
  toolId: String,         // Zu welchem Tool gehört diese Action?
  Rewards: ActionRewards
)

case class ActionRewards(
  cpuGained: Int,
  ramGained: Int,
  codeGained: Int,
  xpGained: Int
)


enum LaptopActionType:
  case PortScan
  case BruteForce
  case Exploit
  case DDoS
  case DataExtraction
  case Backdoor