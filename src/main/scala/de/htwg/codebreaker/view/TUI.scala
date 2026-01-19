// src/main/scala/de/htwg/codebreaker/view/TUI.scala
// $COVERAGE-OFF$
package de.htwg.codebreaker.view

import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.commands.{MovePlayerCommand, HackServerCommand, NextPlayerCommand, UnlockHackSkillCommand, UnlockSocialSkillCommand, StartLaptopActionCommand, CollectLaptopActionResultCommand}
import de.htwg.codebreaker.model.map.{MapObject, Tile}
import de.htwg.codebreaker.model.player.laptop.LaptopAction
import de.htwg.codebreaker.model.server.Server
import de.htwg.codebreaker.model.map.MapObject._

import de.htwg.codebreaker.util.Observer

import scala.io.StdIn
import scala.util.{Failure, Success}
import org.checkerframework.checker.units.qual.s

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
      controller.doAndForget(NextPlayerCommand())

    case "skills" =>  // ← NEU!
      showSkillMenu()

    case "laptop" =>
      showLaptopMenu()

    case s if s.startsWith("move ") =>
      handleMove(s)

    case s if s.startsWith("tile ") =>
      handleTileMenu(s)

    case _ =>
      println("Unbekannter Befehl. 'help' für Hilfe.")
  }

  // =========================
  // LAPTOP MENU
  // =========================

  private def showLaptopMenu(): Unit = {
    val playerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
    val player = controller.getPlayers(playerIndex)

    println("\n" + "=" * 60)
    println(s"${player.name} – Laptop")
    println("=" * 60)

    val runningCount = player.laptop.runningActions.length
    val completedCount = controller.getCompletedActionsForCurrentPlayer().length

    println(s"\na) 🔨 Attack (neue Action starten)")
    println(s"r) 🔄 Running Tasks ($runningCount)")
    println(s"c) ✅ Completed Tasks ($completedCount)")
    println(s"0) Zurück")
    print("> ")

    StdIn.readLine() match {
      case "a" =>
        showAttackMenu()
      case "r" =>
        showRunningActionsMenu()
      case "c" =>
        showCompletedActionsMenu()
      case "0" =>
        println("Zurück zum Hauptmenü.")
      case _ =>
        println("Ungültige Auswahl.")
    }
  }

  // --- ATTACK: Neue Actions starten ---
  private def showAttackMenu(): Unit = {
    val playerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
    val player = controller.getPlayers(playerIndex)
    
    // Hier könnten verfügbare Actions angezeigt werden
    // Für jetzt: einfache Platzhalter-Actions
    println("\n💻 Verfügbare Actions:")
    println("0) Zurück")
    print("> ")
    
    StdIn.readLine() match {
      case "0" => showLaptopMenu()
      case _ => 
        println("Noch keine Actions konfiguriert.")
        showLaptopMenu()
    }
  }

  // --- RUNNING ACTIONS ---
  private def showRunningActionsMenu(): Unit = {
    val playerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
    val player = controller.getPlayers(playerIndex)
    val currentRound = controller.game.state.round
    
    // Nur Actions die noch laufen (nicht fertig)
    val running = player.laptop.runningActions.filter(_.completionRound > currentRound)

    if (running.isEmpty) {
      println("\n🔄 Keine laufenden Actions.")
      showLaptopMenu()
      return
    }

    println("\n🔄 Laufende Actions:")
    println("-" * 60)
    running.zipWithIndex.foreach { case (action, i) =>
      val roundsLeft = action.completionRound - currentRound
      println(s"${i + 1}) ${action.action.name}")
      println(s"   Target: ${action.targetServer}")
      println(s"   Fertig in Runde: ${action.completionRound} (noch $roundsLeft Runden)")
      println(s"   ${action.action.description}")
    }
    println("\n0) Zurück")
    print("> ")

    StdIn.readLine() match {
      case "0" => showLaptopMenu()
      case _ => 
        println("Ungültige Auswahl.")
        showRunningActionsMenu()
    }
  }

  // --- COMPLETED ACTIONS (Mit Claim/Steal Optionen) ---
  private def showCompletedActionsMenu(): Unit = {
    val playerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
    val currentRound = controller.game.state.round
    
    // Nur Actions die fertig sind (completion_round <= currentRound)
    val completed = controller.getCompletedActionsForCurrentPlayer()
      .filter(_.completionRound <= currentRound)

    if (completed.isEmpty) {
      println("\n✅ Keine abgeschlossenen Actions.")
      showLaptopMenu()
      return
    }

    println("\n✅ Abgeschlossene Actions:")
    println("-" * 60)
    completed.zipWithIndex.foreach { case (action, i) =>
      println(s"${i + 1}) ${action.action.name}")
      println(s"   Target: ${action.targetServer}")
      println(s"   ${action.action.description}")
    }
    println("\n0) Zurück")
    print("Wähle eine Action (Nummer): ")

    StdIn.readLine() match {
      case "0" => showLaptopMenu()
      case input =>
        try {
          val idx = input.toInt - 1
          if (idx >= 0 && idx < completed.length) {
            val selectedAction = completed(idx)
            showClaimOrStealMenu(playerIndex, selectedAction.targetServer)
          } else {
            println("Ungültige Auswahl.")
            showCompletedActionsMenu()
          }
        } catch {
          case _: NumberFormatException =>
            println("Ungültige Eingabe.")
            showCompletedActionsMenu()
        }
    }
  }

  // --- CLAIM vs STEAL MENU ---
  private def showClaimOrStealMenu(playerIndex: Int, targetServer: String): Unit = {
    println("\n" + "=" * 60)
    println(s"$targetServer – Reward abholen")
    println("=" * 60)
    println("\n1) 🏆 Claim (Server beanspruchen + Belohnung)")
    println("2) 🔓 Daten klauen (nur Belohnung, Server gehackt aber nicht geclaimed)")
    println("0) Abbrechen")
    print("> ")

    StdIn.readLine() match {
      case "1" =>
        // Claim = true
        controller.doAndRemember(
          CollectLaptopActionResultCommand(playerIndex, targetServer, claimServer = true)
        )
        println("✅ Server geclaimed!")
        showLaptopMenu()

      case "2" =>
        // Steal = false (nur Daten klauen)
        controller.doAndRemember(
          CollectLaptopActionResultCommand(playerIndex, targetServer, claimServer = false)
        )
        println("🔓 Daten geklaut!")
        showLaptopMenu()

      case "0" =>
        println("Abgebrochen.")
        showCompletedActionsMenu()

      case _ =>
        println("Ungültige Auswahl.")
        showClaimOrStealMenu(playerIndex, targetServer)
    }
  }

  // =========================
  // SKILL MENU
  // =========================

  private def showSkillMenu(): Unit = {
    val playerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
    val player = controller.getPlayers(playerIndex)
    val allHackSkills = controller.game.model.hackSkills
    val allSocialSkills = controller.game.model.socialSkills

    println("\n" + "=" * 60)
    println(s"${player.name} – Skilltree (XP: ${player.availableXp})")
    println("=" * 60)

    // === HACK SKILLS ===
    println("\n💻 HACK SKILLS:")
    println("-" * 60)
    allHackSkills.zipWithIndex.foreach { case (skill, idx) =>
      val unlocked = player.skills.unlockedHackSkills.contains(skill.id)
      val status = if (unlocked) "✔" else if (player.availableXp >= skill.costXp) "🔓" else "❌"
      // ← GEÄNDERT: Verwende s"..." statt f"..."
      println(s"${(idx + 1).toString.padTo(2, ' ')} $status ${skill.name.padTo(20, ' ')} | XP: ${skill.costXp.toString.padTo(3, ' ')} | Bonus: +${skill.successBonus}%%")
      println(s"    ${skill.description}")
    }

    // === SOCIAL SKILLS ===
    val socialOffset = allHackSkills.length
    println("\n🗣️ SOCIAL SKILLS:")
    println("-" * 60)
    allSocialSkills.zipWithIndex.foreach { case (skill, idx) =>
      val unlocked = player.skills.unlockedSocialSkills.contains(skill.id)
      val status = if (unlocked) "✔" else if (player.availableXp >= skill.costXp) "🔓" else "❌"
      println(s"${(socialOffset + idx + 1).toString.padTo(2, ' ')} $status ${skill.name.padTo(20, ' ')} | XP: ${skill.costXp.toString.padTo(3, ' ')} | Bonus: +${skill.successBonus}%%")
      println(s"    ${skill.description}")
    }

    println("\n" + "=" * 60)
    println("Wähle einen Skill zum Freischalten (Nummer) oder 0 zum Abbrechen:")
    print("> ")

    StdIn.readLine() match {
      case "0" => 
        println("Abgebrochen.")
      
      case input =>
        try {
          val choice = input.toInt - 1
          
          if (choice >= 0 && choice < allHackSkills.length) {
            // Hack Skill ausgewählt
            val skill = allHackSkills(choice)
            controller.doAndRemember(UnlockHackSkillCommand(playerIndex, skill))
          } else if (choice >= socialOffset && choice < socialOffset + allSocialSkills.length) {
            // Social Skill ausgewählt
            val skill = allSocialSkills(choice - socialOffset)
            controller.doAndRemember(UnlockSocialSkillCommand(playerIndex, skill))
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

      controller.game.model.map.tileAt(x, y) match {
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
      controller.game.model.hackSkills
        .filter(s => player.skills.unlockedHackSkills.contains(s.id))

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

      controller.game.model.map.tileAt(x, y) match {
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
      println(s"  CPU: ${p.laptop.hardware.cpu} | RAM: ${p.laptop.hardware.ram} | Code: ${p.laptop.hardware.code}")
      println(s"  XP: ${p.availableXp} | Total XP: ${p.totalXpEarned}")
      println(s"  HackSkills: ${p.skills.unlockedHackSkills.mkString(", ")}")
      println(s"  SocialSkills: ${p.skills.unlockedSocialSkills.mkString(", ")}")
      println(s"  Movement Points: ${p.movementPoints}/${p.maxMovementPoints}")
      println(s"  Running Actions: ${p.laptop.runningActions.length}")
      println(s"  Completed Actions: ${controller.getCompletedActionsForCurrentPlayer().length}")
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
        |  skills            → Skilltree öffnen
        |  laptop            → Laptop-Menu (Actions & Tasks)
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