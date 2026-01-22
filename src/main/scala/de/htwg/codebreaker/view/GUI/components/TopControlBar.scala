package de.htwg.codebreaker.view.gui.components

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.controller.commands.player.NextPlayerCommand
import de.htwg.codebreaker.view.gui.components.menu.skilltree.SkillTreeWindow
import scalafx.scene.layout.{HBox, Region, Priority}
import scalafx.scene.control.{Button, Label}
import scalafx.beans.property.BooleanProperty
import scala.compiletime.uninitialized

class TopControlBar(
  controller: ControllerInterface,
  config: ViewConfig,
  canUndoProperty: BooleanProperty,
  canRedoProperty: BooleanProperty,
  onPause: () => Unit
) {
  
  // ⚡ Speichere Referenzen für refresh()
  private var topBarBox: HBox = uninitialized
  private var roundLabel: Label = uninitialized
  private var currentPlayerLabel: Label = uninitialized

  /**
   * Erstellt die TopBar initial.
   */
  def createTopBar(): HBox = {
    topBarBox = buildTopBarContent()
    topBarBox
  }
  
  /**
   * ⚡ REFRESH: Nur Labels aktualisieren!
   */
  def refresh(): Unit = {
    if (roundLabel != null && currentPlayerLabel != null) {
      roundLabel.text = s"Runde: ${controller.getState.round}"
      
      val currentPlayer = controller.getPlayers(controller.getState.currentPlayerIndex.getOrElse(0))
      currentPlayerLabel.text = s"Am Zug: ${currentPlayer.name}"
    }
  }
  
  /**
   * Baut den TopBar-Inhalt.
   */
  private def buildTopBarContent(): HBox = {
    roundLabel = createRoundLabel()
    currentPlayerLabel = createCurrentPlayerLabel()
    
    new HBox {
      style = s"-fx-background-color: #222; -fx-padding: ${config.spacing}; -fx-spacing: ${config.spacing * 2}; -fx-alignment: center-left;"
      children = Seq(
        createSkillTreeButton(),
        roundLabel,
        currentPlayerLabel,
        createSpacer(),
        createUndoButton(),
        createRedoButton(),
        createNextPlayerButton(),
        createPauseButton()
      )
    }
  }

  private def createSkillTreeButton(): Button = {
    new Button("🌳 Skills") {
      onAction = _ => {
        new SkillTreeWindow(controller).show()
      }
      style = s"-fx-background-color: #8844cc; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeSmall}px;"
    }
  }
  
  private def createRoundLabel(): Label = {
    new Label(s"Runde: ${controller.getState.round}") {
      style = s"-fx-text-fill: #fff; -fx-font-size: ${config.fontSizeLarge}px; -fx-font-weight: bold;"
    }
  }
  
  private def createCurrentPlayerLabel(): Label = {
    val currentPlayer = controller.getPlayers(controller.getState.currentPlayerIndex.getOrElse(0))
    new Label(s"Am Zug: ${currentPlayer.name}") {
      style = s"-fx-text-fill: #4db8ff; -fx-font-size: ${config.fontSizeLarge}px; -fx-font-weight: bold;"
    }
  }
  
  private def createSpacer(): Region = {
    new Region {
      hgrow = Priority.Always
    }
  }
  
  private def createUndoButton(): Button = {
    new Button("Rückgängig") {
      onAction = _ => controller.undo()
      disable <== canUndoProperty.not()
      style = s"-fx-background-color: #ffcc00; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeSmall}px;"
    }
  }
  
  private def createRedoButton(): Button = {
    new Button("Wiederholen") {
      onAction = _ => controller.redo()
      disable <== canRedoProperty.not()
      style = s"-fx-background-color: #66ff66; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeSmall}px;"
    }
  }
  
  private def createNextPlayerButton(): Button = {
    new Button("Runde an nächsten Spieler") {
      onAction = _ => controller.doAndForget(NextPlayerCommand())
      style = s"-fx-background-color: #4db8ff; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeSmall}px;"
    }
  }
  
  private def createPauseButton(): Button = {
    new Button("⚙️") {
      onAction = _ => onPause()
      style = s"-fx-background-color: #555; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeSmall}px;"
    }
  }
}