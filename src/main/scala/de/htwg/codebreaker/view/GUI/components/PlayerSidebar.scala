package de.htwg.codebreaker.view.gui.components

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.server.Server
import de.htwg.codebreaker.view.gui.components.ViewConfig
import de.htwg.codebreaker.view.gui.components.menu.playerActionMenu.LaptopMainMenu
import scalafx.scene.layout.{VBox, HBox}
import scalafx.scene.control.{Label, Button}
import scalafx.geometry.Insets

/**
 * Seitenleiste mit Informationen zum aktuellen Spieler und seinen geclaimten Servern.
 */
class PlayerSidebar(
  controller: ControllerInterface,
  config: ViewConfig
) {
  
  def createSidebar(): VBox = {
    val currentPlayerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
    val currentPlayer = controller.getPlayers(currentPlayerIndex)
    val claimedServers = getClaimedServers(currentPlayerIndex)
    
    new VBox {
      style = s"-fx-background-color: #1a1a1a; -fx-padding: ${config.padding}; -fx-min-width: ${config.sidebarWidth}; -fx-max-width: ${config.sidebarWidth};"
      spacing = config.spacing * 1.5
      children = Seq(
        createPlayerInfoBox(currentPlayer, currentPlayerIndex),
        createClaimedServersSection(claimedServers)
      )
    }
  }
  
  private def createPlayerInfoBox(player: Player, index: Int): VBox = {
    val playerColor = if (index == 0) "#4db8ff" else "#ff6666"
    
    new VBox {
      style = s"-fx-background-color: #2a2a2a; -fx-padding: ${config.spacing}; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: $playerColor; -fx-border-width: 2;"
      spacing = config.spacing * 0.5
      children = Seq(
        new Label(s"${player.name} ★") {
          style = s"-fx-text-fill: $playerColor; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeMedium * 1.2}px;"
        },
        createResourceRow(player),
        createMovementLabel(player),
        createLevelXpRow(player),
        createLaptopButton(index)
      )
    }
  }
  
  private def createResourceRow(player: Player): HBox = {
    new HBox {
      spacing = config.spacing
      children = Seq(
        new Label(s"CPU: ${player.laptop.hardware.cpu}") {
          style = s"-fx-text-fill: #66ff66; -fx-font-size: ${config.fontSizeSmall}px;"
        },
        new Label(s"RAM: ${player.laptop.hardware.ram}") {
          style = s"-fx-text-fill: #66ccff; -fx-font-size: ${config.fontSizeSmall}px;"
        },
        new Label(s"Code: ${player.laptop.hardware.code}") {
          style = s"-fx-text-fill: #ffcc66; -fx-font-size: ${config.fontSizeSmall}px;"
        }
      )
    }
  }
  
  private def createMovementLabel(player: Player): Label = {
    new Label(s"Bewegung: ${player.movementPoints}/${player.maxMovementPoints}") {
      style = s"-fx-text-fill: #4db8ff; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeSmall}px;"
    }
  }
  
  private def createLevelXpRow(player: Player): HBox = {
    new HBox {
      spacing = config.spacing
      children = Seq(
        new Label(s"Total XP: ${player.totalXpEarned}") {
          style = s"-fx-text-fill: #ccc; -fx-font-size: ${config.fontSizeSmall}px;"
        },
        new Label(s"Available XP: ${player.availableXp}") {
          style = s"-fx-text-fill: #ffb84d; -fx-font-size: ${config.fontSizeSmall}px;"
        }
      )
    }
  }
  
  private def createLaptopButton(playerIndex: Int): Button = {
    new Button("💻 Laptop öffnen") {
      style = "-fx-font-size: 12px; -fx-padding: 10; -fx-background-color: #7b68ee; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-cursor: hand;"
      maxWidth = Double.MaxValue
      onAction = _ => {
        // Öffne das neue Laptop Hauptmenü
        new LaptopMainMenu(controller, playerIndex).show()
      }
    }
  }
  
  private def getClaimedServers(playerIndex: Int): List[Server] = {
    controller.getServers.filter(_.claimedBy.contains(playerIndex))
  }
  
  private def createClaimedServersSection(servers: List[Server]): VBox = {
    val header = new Label("═══ Geclaimed Server ═══") {
      style = s"-fx-text-fill: #4db8ff; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeMedium}px;"
    }
    
    if (servers.isEmpty) {
      new VBox {
        spacing = config.spacing * 0.5
        children = Seq(
          header,
          new Label("Keine Server geclaimed") {
            style = s"-fx-text-fill: #888; -fx-font-size: ${config.fontSizeSmall}px; -fx-padding: ${config.spacing};"
          }
        )
      }
    } else {
      val serverBoxes = servers.map(createServerBox)
      
      new VBox {
        spacing = config.spacing * 0.5
        children = Seq(header) ++ serverBoxes
      }
    }
  }
  
  private def createServerBox(server: Server): VBox = {
    val typeColor = server.serverType.toString match {
      case "Side" => "#66ff66"
      case "Firm" => "#66ccff"
      case "Cloud" => "#ffcc66"
      case "Bank" => "#ffb84d"
      case "Military" => "#ff6666"
      case "GKS" => "#ff99ff"
      case _ => "#888"
    }
    
    new VBox {
      style = s"-fx-background-color: #222; -fx-padding: ${config.spacing * 0.7}; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: $typeColor; -fx-border-width: 1;"
      spacing = config.spacing * 0.3
      children = Seq(
        new Label(s"💻 ${server.name}") {
          style = s"-fx-text-fill: $typeColor; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeSmall}px;"
        },
        new Label(s"${server.serverType} (${server.difficulty}%)") {
          style = s"-fx-text-fill: #ccc; -fx-font-size: ${config.fontSizeSmall * 0.9}px;"
        }
      )
    }
  }
}