package de.htwg.codebreaker.view.gui.components.menu.playeractionmenu

import de.htwg.codebreaker.controller.{ControllerInterface, MovePlayerCommand, HackServerCommand}
import de.htwg.codebreaker.model.Tile
import scalafx.scene.control.{ContextMenu, MenuItem}
import scala.util.{Success, Failure}

class TileActionMenu(
  controller: ControllerInterface,
  tile: Tile,
  playerIndex: Int
) {

  def create(): ContextMenu = {
    val menu = new ContextMenu()

    // ➡ Bewegung - Backend fragen ob möglich
    val moveCommand = MovePlayerCommand(playerIndex, tile)
    val moveResult = moveCommand.doStep(controller.game)
    
    val moveItem = moveResult match {
      case Success(_) =>
        val item = new MenuItem("➡ Bewegen")
        item.onAction = _ => controller.doAndRemember(moveCommand)
        item
        
      case Failure(e) =>
        val item = new MenuItem(s"➡ Bewegen (${extractShortReason(e.getMessage)})")
        item.disable = true
        item
    }
    menu.items.add(moveItem)

    // 💻 Server auf diesem Tile - Backend fragen ob hackbar
    controller.getServers
      .find(_.tile == tile)
      .foreach { server =>
        
        val hackCommand = HackServerCommand(server.name, playerIndex)
        val hackResult = hackCommand.doStep(controller.game)
        
        val hackItem = hackResult match {
          case Success(_) =>
            val item = new MenuItem(s"💻 ${server.name} hacken")
            item.onAction = _ => controller.doAndRemember(hackCommand)
            item
            
          case Failure(e) =>
            val item = new MenuItem(s"💻 ${server.name} (${extractShortReason(e.getMessage)})")
            item.disable = true
            item
        }
        
        menu.items.add(hackItem)
      }

    // ℹ Info
    val infoItem = new MenuItem(s"ℹ Feldinfo (${tile.x}, ${tile.y})")
    infoItem.onAction = _ => showTileInfo()
    menu.items.add(infoItem)

    menu
  }

  /**
   * Extrahiert kurze, user-freundliche Fehlermeldung
   */
  private def extractShortReason(fullMessage: String): String = {
    fullMessage match {
      case msg if msg.contains("Nicht genug Bewegungspunkte") =>
        val remaining = msg.split("vorhanden: ").lastOption.getOrElse("?").stripSuffix(")")
        s"${remaining} MP"
      case msg if msg.contains("Nicht genug CPU") =>
        val available = msg.split("vorhanden: ").lastOption.getOrElse("?").stripSuffix(")")
        s"CPU ${available}"
      case msg if msg.contains("Nicht genug RAM") =>
        val available = msg.split("vorhanden: ").lastOption.getOrElse("?").stripSuffix(")")
        s"RAM ${available}"
      case msg if msg.contains("bereits gehackt") => "gehackt"
      case msg if msg.contains("Private Server") => "privat"
      case msg if msg.contains("muss auf Server-Tile sein") => "nicht hier"
      case msg if msg.contains("Ocean-Tile") => "Ocean"
      case msg if msg.contains("muss sich von aktuellem") => "hier"
      case _ => "n.m."
    }
  }

  /**
   * Zeigt detaillierte Tile-Informationen
   */
  private def showTileInfo(): Unit = {
    val info = new StringBuilder()
    info.append(s"📍 Position: (${tile.x}, ${tile.y})\n")
    info.append(s"🌍 Kontinent: ${tile.continent}\n")
    
    controller.getServers.find(_.tile == tile).foreach { server =>
      info.append(s"\n💻 Server: ${server.name}\n")
      info.append(s"   Typ: ${server.serverType}\n")
      info.append(s"   Schwierigkeit: ${server.difficulty}\n")
      info.append(s"   Status: ${if (server.hacked) "gehackt" else "aktiv"}\n")
    }
    
    println(info.toString())
  }
}