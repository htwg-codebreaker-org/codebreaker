package de.htwg.codebreaker.view.gui.components.menu.pauseMenu

import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.VBox
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}

class PauseMenu(
  onResume: () => Unit,
  onSave: () => Unit,
  onExit: () => Unit
) {

  def show(): Unit = {
    val stage = new Stage {
      title = "Pause"
      resizable = false
    }

    val resumeButton = new Button("▶ Spiel fortsetzen") {
      style = "-fx-font-weight: bold;"
      onAction = _ => {
        stage.close()
        onResume()
      }
    }

    val exitButton = new Button("❌ Spiel beenden & Speichern") {
      style = "-fx-font-weight: bold;"
      onAction = _ => {
        onSave()
        stage.close()
        onExit()
      }
    }

    stage.scene = new Scene(
      new VBox {
        spacing = 15
        padding = Insets(20)
        alignment = Pos.Center
        children = Seq(resumeButton, exitButton)
      }
    )

    stage.show()
  }
}
