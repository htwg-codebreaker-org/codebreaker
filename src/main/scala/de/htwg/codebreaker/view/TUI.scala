// src/main/scala/de/htwg/codebreaker/view/TUI.scala
// $COVERAGE-OFF$
package de.htwg.codebreaker.view

import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.commands._
import de.htwg.codebreaker.model.{Server, ServerType, Continent}
import de.htwg.codebreaker.model.MapObject
import de.htwg.codebreaker.model.MapObject._
import de.htwg.codebreaker.util.Observer

import scala.io.StdIn
import scala.util.{Failure, Success}

/**
 * Text-based User Interface (TUI)
 * - Tile-basierte Aktionen
 * - Hacken mit Skill-Auswahl
 */
class TUI @Inject()(controller: ControllerInterface)
  extends Observer
    with LazyLogging {

  controller.add(this)
  logger.info("TUI initialized")

  // =========================
  // INPUT HANDLING
  // =========================

  def processInputLine(input: String): Unit =
    input.trim match {

      case "q" =>
        println("Spiel beendet.")

      case "m" =>
        show()

      case "help" =>
        printHelp()

      case "undo" =>
        controller.undo()

      case "redo" =>
        controller.redo()

      case "next" =>
        controller.doAndRemember(NextPlayerCommand())

      case s if s.startsWith("move ") =>
        handleMove(s)

      case s if s.startsWith("tile ") =>
        handleTileMenu(s)

      case _ =>
        println("Unbekannter Befehl. 'help' für Hilfe.")
    }

  // =========================
  // TILE MENU
  // =========================

  private def handleTileMenu(input: String): Unit = {
    val parts = input.split(" ")
    if (parts.length != 3) {
      println("Syntax: tile <X> <Y>")
      return
    }

    try {
      val x = parts(1).toInt
      val y = parts(2).toInt

      val playerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
      val player = controller.getPlayers(playerIndex)

      controller.game.model.worldMap.tileAt(x, y) match {
        case None =>
          println("Ungültiges Tile.")

        case Some(tile) =>
          println(s"\n📍 Tile (${tile.x}, ${tile.y}) [${tile.continent}]")

          // ---- MOVE OPTION ----
          val moveCmd = MovePlayerCommand(playerIndex, tile)
          moveCmd.doStep(controller.game) match {
            case Success(_) =>
              println("1) ➡ Bewegen")
            case Failure(_) =>
              println("1) ➡ Bewegen (nicht möglich)")
          }

          // ---- HACK OPTION ----
          controller.getServers.find(_.tile == tile) match {
            case Some(server) if !server.hacked =>
              println("2) 💻 Server angreifen")
            case Some(_) =>
              println("2) 💻 Server (bereits gehackt)")
            case None =>
              println("2) 💻 Kein Server")
          }

          println("0) Abbrechen")
          print("> ")

          StdIn.readLine() match {
            case "1" =>
              controller.doAndRemember(moveCmd)

            case "2" =>
              controller.getServers.find(_.tile == tile).foreach { server =>
                openHackMenu(server, playerIndex)
              }

            case _ =>
              println("Abgebrochen.")
          }
      }

    } catch {
      case _: NumberFormatException =>
        println("X und Y müssen Zahlen sein.")
    }
  }

  // =========================
  // HACK MENU (SKILLS)
  // =========================

  private def openHackMenu(server: Server, playerIndex: Int): Unit = {
    val player = controller.getPlayers(playerIndex)

    val unlockedSkills =
      controller.game.model.skills
        .filter(s => player.skills.unlockedSkillIds.contains(s.id))

    if (unlockedSkills.isEmpty) {
      println("❌ Keine Skills freigeschaltet.")
      return
    }

    println(s"\n💻 Angriff auf ${server.name}")
    unlockedSkills.zipWithIndex.foreach { case (skill, i) =>
      val chance =
        math.min(95, (100 - server.difficulty) + skill.successBonus)
      println(s"${i + 1}) ${skill.name}  → $chance%")
    }
    println("0) Abbrechen")
    print("> ")

    StdIn.readLine() match {
      case "0" => println("Abgebrochen.")
      case s =>
        try {
          val idx = s.toInt - 1
          if (idx >= 0 && idx < unlockedSkills.size) {
            val skill = unlockedSkills(idx)
            controller.doAndRemember(
              HackServerCommand(server.name, playerIndex, skill)
            )
          } else {
            println("Ungültige Auswahl.")
          }
        } catch {
          case _: NumberFormatException =>
            println("Ungültige Eingabe.")
        }
    }
  }

  // =========================
  // MOVE
  // =========================

  private def handleMove(input: String): Unit = {
    val parts = input.split(" ")
    if (parts.length != 3) {
      println("Syntax: move <X> <Y>")
      return
    }

    try {
      val x = parts(1).toInt
      val y = parts(2).toInt
      val playerIndex = controller.getState.currentPlayerIndex.getOrElse(0)

      controller.game.model.worldMap.tileAt(x, y) match {
        case Some(tile) =>
          controller.doAndRemember(
            MovePlayerCommand(playerIndex, tile)
          )
        case None =>
          println("Ungültige Koordinaten.")
      }
    } catch {
      case _: NumberFormatException =>
        println("X und Y müssen Zahlen sein.")
    }
  }

  // =========================
  // OUTPUT
  // =========================

  def show(): Unit = {
    val players = controller.getPlayers
    val map = controller.getMapData()

    println("\n== Codebreaker ==")
    println(displayMap(map))
    println()

    players.zipWithIndex.foreach { case (p, i) =>
      println(s"Spieler $i: ${p.name}")
      println(s"  Position: (${p.tile.x}, ${p.tile.y})")
      println(s"  CPU: ${p.cpu} | RAM: ${p.ram} | Code: ${p.code}")
      println(s"  XP: ${p.availableXp} | Total XP: ${p.totalXpEarned}")
      println(s"  Skills: ${p.skills.unlockedSkillIds.mkString(", ")}")
      println("-" * 30)
    }
  }

  def displayMap(mapData: Vector[Vector[MapObject]]): String =
    val BLUE = "\u001B[34m"
    val GREEN = "\u001B[32m"
    val RED = "\u001B[31m"
    val RESET = "\u001B[0m"

    mapData.map { row =>
      row.map {
        case PlayerAndServerTile(p, s, _, _) =>
          val content = f"P$p%d$s%02d"
          s"$RED$content$RESET"
        case PlayerOnTile(i) =>
          val content = f"P$i%d"
          s"$BLUE$content$RESET"
        case ServerOnTile(i, _, _) =>
          val content = f"$i%02d"
          s"$GREEN$content$RESET"
        case EmptyTile(cont) =>
          cont.short.take(2)
      }.mkString(" ")
    }.mkString("\n") 

  private def printHelp(): Unit =
    println(
      """Befehle:
        |  m                 → Karte anzeigen
        |  tile <x> <y>      → Aktionen für Tile
        |  move <x> <y>      → Spieler bewegen
        |  undo / redo
        |  next              → Nächster Spieler
        |  q                 → Beenden
        |""".stripMargin
    )

  override def update(): Unit = {
    println("\n🔄 Spielzustand geändert")
    show()
  }
}
// $COVERAGE-ON$