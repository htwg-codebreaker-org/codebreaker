package ui

import model._

object TUI:

  def show(players: List[Player], map: WorldMap, servers: List[Server]): Unit =
    println("Willkommen zu Codebreaker!")
    println("== Codebreaker: Weltkarte ==")
    println(map.display(players, servers))
    println()
    players.zipWithIndex.foreach { case (p, i) =>
      println(s"Spieler $i: ${p.name} @ ${p.position}")
      println(s"CPU: ${p.cpu} | RAM: ${p.ram} | Code: ${p.code}")
      println(s"Level: ${p.level} | XP: ${p.xp} | Security: ${p.cybersecurity}%")
      println("-" * 30)
    }
    println("== Serverliste ==")
    printServerList(servers, map)

  def printServerList(servers: List[Server], map: WorldMap): Unit =
    servers.zipWithIndex.foreach { case (server, index) =>
      val pos = server.position
      val continent = map.continentAt(pos._1, pos._2)
      val contShort = continent.map(_.short).getOrElse("??")

      val rewardStr = server.serverType match
        case ServerType.Side    => s"+${server.rewardCpu} CPU, +${server.rewardRam} RAM"
        case ServerType.Bank    => s"+${server.rewardCpu} CPU, +${server.rewardRam} Code"
        case ServerType.GKS     => "🏁 Endziel"
        case _                  => s"+${server.rewardCpu} CPU, +${server.rewardRam} RAM"

      println(f"[$index%2d] ${server.name}%-20s | $contShort | Pos: (${pos._1}%2d, ${pos._2}%2d) | Typ: ${server.serverType}%-9s | Schwierigkeit: ${server.difficulty}%2d%% | $rewardStr")
    }
