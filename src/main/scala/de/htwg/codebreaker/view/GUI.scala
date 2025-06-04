package de.htwg.codebreaker.view

import de.htwg.codebreaker.controller.Controller
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox
import scalafx.Includes._
import scala.compiletime.uninitialized

object GUI extends JFXApp3:

  var controller: Controller = uninitialized

  override def start(): Unit =
    val showMapButton = new Button("Zeige Weltkarte")
    showMapButton.onAction = _ => println("Weltkarte anzeigen: (GUI folgt...)")

    stage = new JFXApp3.PrimaryStage:
      title = "Codebreaker GUI"
      width = 400
      height = 300
      scene = new Scene:
        root = new VBox:
          spacing = 10
          children = Seq(
            new Label("Willkommen zu Codebreaker!"),
            showMapButton
          )
