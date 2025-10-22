// src/main/scala/de/htwg/codebreaker/view/TUI.scala
package de.htwg.codebreaker.view

import de.htwg.codebreaker.controller.Controller
import de.htwg.codebreaker.util.Observer
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.MapObject._
import de.htwg.codebreaker.controller.ClaimServerCommand
import de.htwg.codebreaker.controller.NextPlayerCommand

class TUI(controller: Controller) extends Observer:
  controller.add(this)

  def processInputLine(input: String): Unit =
    input.trim match
      case "q" =>
        println("Spiel beendet.")

      case "m" =>
        show()

      case "undo" =>
        controller.undo()

      case "redo" =>
        controller.redo()

      case "next" =>
        controller.doAndRemember(NextPlayerCommand())

      case "help" =>
        println(
          """Verfügbare Befehle:
            |  m            → Karte & Status anzeigen
            |  claim <S>    → Server S claimen
            |  undo         → Letzten Spielzug rückgängig
            |  redo         → Rückgängig gemachten Zug wiederholen
            |  next         → Nächster Spieler
            |  q            → Spiel beenden
            |""".stripMargin)


      case "claim" =>
        println("Syntax: claim <Servername>")

      case s if s.startsWith("claim ") =>
        val parts = s.split(" ")
        if parts.length == 2 then
          val serverName = parts(1)
          val playerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
          controller.doAndRemember(ClaimServerCommand(serverName, playerIndex))
        else
          println("Syntax: claim <Servername>")


      case _ =>
        println("Unbekannter Befehl.")



  def show(): Unit =
    val players = controller.getPlayers
    val servers = controller.getServers
    val map = controller.getMapData()

    println("Willkommen zu Codebreaker!")
    println("== Codebreaker: Weltkarte ==")
    println(displayMap(map))
    println()
    players.zipWithIndex.foreach { case (p, i) =>
      println(s"Spieler $i: ${p.name} @ (${p.tile.x}, ${p.tile.y}) [${p.tile.continent.short}]")
      println(s"CPU: ${p.cpu} | RAM: ${p.ram} | Code: ${p.code}")
      println(s"Level: ${p.level} | XP: ${p.xp} | Security: ${p.cybersecurity}%")
      println("-" * 30)
    }
    println("== Serverliste ==")
    printServerList(servers)

  def displayMap(mapData: Vector[Vector[MapObject]]): String =
    val BLUE = "\u001B[34m"
    val GREEN = "\u001B[32m"
    val RED = "\u001B[31m"
    val RESET = "\u001B[0m"
    
    mapData.map { row =>
      row.map {
        case PlayerAndServerTile(p, s, _, _) => f"$RED[P$p/S$s]$RESET"
        case PlayerOnTile(i)                 => f"$BLUE[P$i]$RESET"
        case ServerOnTile(i, _, _)          => f"$GREEN[$i]-S$RESET"
        case EmptyTile(cont)                    => f"${cont.short}"
      }.mkString(" ")
    }.mkString("\n")

  def printServerList(servers: List[Server]): Unit =
    servers.zipWithIndex.foreach { case (server, index) =>
      val tile = server.tile
      val contShort = tile.continent.short
      val pos = (tile.x, tile.y)
      val rewardStr = server.serverType match
        case ServerType.Side    => s"+${server.rewardCpu} CPU, +${server.rewardRam} RAM"
        case ServerType.Bank    => s"+${server.rewardCpu} CPU, +${server.rewardRam} Code"
        case ServerType.GKS     => "🏁 Endziel"
        case _                  => s"+${server.rewardCpu} CPU, +${server.rewardRam} RAM"
      println(f"[$index%2d] ${server.name}%-20s | $contShort | Pos: (${pos._1}%2d, ${pos._2}%2d) | Typ: ${server.serverType}%-9s | Schwierigkeit: ${server.difficulty}%2d%% | $rewardStr")
    }


  override def update(): Unit =
    println("Spielzustand hat sich geändert.")
    show()