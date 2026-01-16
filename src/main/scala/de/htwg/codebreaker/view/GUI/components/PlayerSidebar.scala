package de.htwg.codebreaker.view.gui.components

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.model.Player
import scalafx.scene.layout.{VBox, HBox}
import scalafx.scene.control.Label

/**
 * Seitenleiste mit Informationen zu allen Spielern.
 */
class PlayerSidebar(
  controller: ControllerInterface,
  config: ViewConfig
) {
  
  def createSidebar(): VBox = {
    val players = controller.getPlayers
    val currentPlayerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
    
    val playerInfoBoxes = players.zipWithIndex.map { case (player, index) =>
      createPlayerInfoBox(player, index, currentPlayerIndex)
    }
    
    new VBox {
      style = s"-fx-background-color: #1a1a1a; -fx-padding: ${config.padding}; -fx-min-width: ${config.sidebarWidth}; -fx-max-width: ${config.sidebarWidth};"
      spacing = config.spacing
      children = playerInfoBoxes
    }
  }
  
  private def createPlayerInfoBox(player: Player, index: Int, currentPlayerIndex: Int): VBox = {
    val isActive = index == currentPlayerIndex
    val borderColor = if (isActive) "#4db8ff" else "#888"
    val backgroundColor = if (isActive) "#2a2a2a" else "#222"
    val playerColor = if (index == 0) "Blue" else "Red"
    
    new VBox {
      style = s"-fx-background-color: $backgroundColor; -fx-padding: ${config.spacing}; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: $borderColor; -fx-border-width: 2;"
      spacing = config.spacing * 0.4
      children = Seq(
        createPlayerNameLabel(player, index, isActive, playerColor),
        createResourceRow(player),
        createMovementLabel(player),
        createLevelXpRow(player),
        createSecurityLabel(player)
      )
    }
  }
  
  private def createPlayerNameLabel(player: Player, index: Int, isActive: Boolean, color: String): Label = {
    new Label(s"${player.name} ${if (isActive) "★" else ""}") {
      style = s"-fx-text-fill: $color; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeMedium}px;"
    }
  }
  
  private def createResourceRow(player: Player): HBox = {
    new HBox {
      spacing = config.spacing
      children = Seq(
        new Label(s"CPU: ${player.cpu}") {
          style = s"-fx-text-fill: #66ff66; -fx-font-size: ${config.fontSizeSmall}px;"
        },
        new Label(s"RAM: ${player.ram}") {
          style = s"-fx-text-fill: #66ccff; -fx-font-size: ${config.fontSizeSmall}px;"
        },
        new Label(s"Code: ${player.code}") {
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
  
  private def createSecurityLabel(player: Player): Label = {
    new Label(s"Security: ${player.cybersecurity}%") {
      style = s"-fx-text-fill: #ff6666; -fx-font-size: ${config.fontSizeSmall}px;"
    }
  }
}