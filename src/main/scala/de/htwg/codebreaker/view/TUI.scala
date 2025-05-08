// package de.htwg.codebreaker.view
// import de.htwg.codebreaker.model._
//   import de.htwg.codebreaker.model.MapObject._

// object TUI:



//   def displayMap(map: WorldMap, players: List[Player], servers: List[Server]): String =
//     val mapData = map.getMapData(players, servers)

//     mapData.map { row =>
//       row.map {
//         case PlayerAndServerTile(p, s, _, cont) => f"[P$p/S$s]"
//         case PlayerOnTile(i)                          => f"[P$i]"
//         case ServerOnTile(i, _, cont) => f"[$i]S-${cont.short}"
//         case EmptyTile(_)                             => " .   "
//       }.mkString(" ")
//     }.mkString("\n")


//   def show(players: List[Player], map: WorldMap, servers: List[Server]): Unit =
//     println("Willkommen zu Codebreaker!")
//     println("== Codebreaker: Weltkarte ==")
//     println(displayMap(map, players, servers))
//     println()
//     players.zipWithIndex.foreach { case (p, i) =>
//       println(s"Spieler $i: ${p.name} @ (${p.tile.x}, ${p.tile.y}) [${p.tile.continent.short}]")
//       println(s"CPU: ${p.cpu} | RAM: ${p.ram} | Code: ${p.code}")
//       println(s"Level: ${p.level} | XP: ${p.xp} | Security: ${p.cybersecurity}%")
//       println("-" * 30)
//     }
//     println("== Serverliste ==")
//     printServerList(servers, map)

//   def printServerList(servers: List[Server], map: WorldMap): Unit =
//     servers.zipWithIndex.foreach { case (server, index) =>
//       val tile = server.tile
//       val contShort = tile.continent.short
//       val pos = (tile.x, tile.y)

//       val rewardStr = server.serverType match
//         case ServerType.Side    => s"+${server.rewardCpu} CPU, +${server.rewardRam} RAM"
//         case ServerType.Bank    => s"+${server.rewardCpu} CPU, +${server.rewardRam} Code"
//         case ServerType.GKS     => "🏁 Endziel"
//         case _                  => s"+${server.rewardCpu} CPU, +${server.rewardRam} RAM"

//       println(f"[$index%2d] ${server.name}%-20s | $contShort | Pos: (${pos._1}%2d, ${pos._2}%2d) | Typ: ${server.serverType}%-9s | Schwierigkeit: ${server.difficulty}%2d%% | $rewardStr")
//     }

