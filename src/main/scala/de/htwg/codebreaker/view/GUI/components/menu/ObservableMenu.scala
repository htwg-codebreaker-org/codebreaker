// src/main/scala/de/htwg/codebreaker/view/gui/components/menu/ObservableWindow.scala
package de.htwg.codebreaker.view.gui.components.menu

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.util.Observer
import scalafx.application.Platform
import scalafx.stage.Stage

trait ObservableWindow extends Observer {
  protected val controller: ControllerInterface
  protected var currentStage: Option[Stage] = None
  
  /**
   * Zeigt das Fenster an und registriert es als Observer
   */
  def show(): Unit = {
    controller.add(this)
    val stage = createStage()
    currentStage = Some(stage)
    refreshContent()
    stage.show()
  }
  
  /**
   * Erstellt die Stage mit grundlegenden Einstellungen
   */
  protected def createStage(): Stage = {
    new Stage {
      onCloseRequest = _ => {
        controller.remove(ObservableWindow.this)
        currentStage = None
      }
    }
  }
  
  /**
   * Observer-Update: Wird aufgerufen wenn sich der Spielzustand ändert
   */
  override def update(): Unit = {
    currentStage.foreach { _ =>
      Platform.runLater {
        refreshContent()
      }
    }
  }
  
  /**
   * Aktualisiert den Fensterinhalt - muss von Subklassen implementiert werden
   */
  protected def refreshContent(): Unit
}