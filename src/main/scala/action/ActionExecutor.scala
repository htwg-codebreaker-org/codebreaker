// src/main/scala/action/ActionExecutor.scala
package action

import model._
import action._

object ActionExecutor:

  type GameResult = (Player, List[GameEvent])

  def executeLocalAction(player: Player, action: LocalAction, server: Server): GameResult =
    action match
      case LocalAction.Infiltrate(_) =>
        val success = chance(70)
        if success then (player.copy(xp = player.xp + 20), Nil)
        else (player.copy(cpu = (player.cpu - 10).max(0)), List(GameEvents.Rückverfolgung))

      case LocalAction.InteractWithEmployee(_) =>
        val success = chance(60)
        if success then (player.copy(xp = player.xp + 15), List(GameEvents.SocialBoost))
        else (player, Nil)

      case LocalAction.DirectHack(_, method) =>
        val baseChance = baseSuccessChance(method, player.level)
        val success = chance(baseChance + 40 - server.difficulty)
        if success then (applyHackReward(player, server), List(GameEvents.Datenleck))
        else (player.copy(cpu = (player.cpu - 20).max(0)), List(GameEvents.Honeypot))


  def executeGlobalAction(player: Player, action: GlobalAction, serverOpt: Option[Server] = None): GameResult =
    action match
      case GlobalAction.Travel(to) =>
        val travelCost = 10
        if player.money >= travelCost then
          (player.copy(position = to, money = player.money - travelCost), List())
        else
          (player, List(GameEvents.NotEnoughMoney))


      case GlobalAction.RemoteHack(_, method) =>
        serverOpt match
          case Some(server) =>
            val baseChance = baseSuccessChance(method, player.level)
            val success = chance(baseChance - server.difficulty)
            if success then (applyHackReward(player, server), Nil)
            else (player.copy(cpu = (player.cpu - 15).max(0)), List(GameEvents.Rückverfolgung))
          case None => (player, Nil)

      case GlobalAction.UpgradeCybersecurity =>
        (player.copy(cybersecurity = (player.cybersecurity + 10).min(100), xp = player.xp + 10), Nil)

      case GlobalAction.HackAirport(_) =>
        val success = chance(50)
        if success then (player.copy(xp = player.xp + 25), Nil)
        else (player.copy(cpu = (player.cpu - 10).max(0)), List(GameEvents.Rückverfolgung))


  private def baseSuccessChance(method: AttackMethod, level: Int): Int =
    val base = method match
      case AttackMethod.BruteForce      => 50
      case AttackMethod.Phishing        => 40
      case AttackMethod.SQLInjection    => 60
      case AttackMethod.DDoS            => 55
      case AttackMethod.Keylogger       => 50
      case AttackMethod.Wurm            => 65
      case AttackMethod.ManInTheMiddle  => 60
      case AttackMethod.ZeroDay         => 75
    base + (level * 5)

  private def applyHackReward(player: Player, server: Server): Player =
    player.copy(
      cpu = player.cpu + server.rewardCpu,
      ram = player.ram + server.rewardRam,
      code = player.code + server.rewardCode,
      money = player.money + server.rewardMoney,
      xp = player.xp + server.rewardXp
    )


  private def chance(percent: Int): Boolean =
    val r = scala.util.Random.nextInt(100)
    r < percent
