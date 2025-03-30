// src/main/scala/action/PlayerAction.scala
package action

import model.AttackMethod

enum LocalAction:
  case Infiltrate(serverId: Int)
  case InteractWithEmployee(serverId: Int)
  case DirectHack(serverId: Int, method: AttackMethod)

enum GlobalAction:
  case Travel(to: (Int, Int))
  case RemoteHack(serverId: Int, method: AttackMethod)
  case UpgradeCybersecurity
  case HackAirport(airportId: Int)