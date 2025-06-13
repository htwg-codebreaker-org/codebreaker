package de.htwg.codebreaker.view

import de.htwg.codebreaker.controller.Controller
import de.htwg.codebreaker.model.{WorldMap, Continent}
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{VBox, GridPane, StackPane, Pane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.input.MouseEvent
import scalafx.Includes._
import scala.compiletime.uninitialized
import scalafx.scene.image.Image
import scalafx.scene.image.ImageView
import java.io.FileInputStream
import scalafx.beans.binding.Bindings

object GUI extends JFXApp3:

  var controller: Controller = uninitialized

  def continentColor(continent: Continent): Color = continent match {
    case Continent.NorthAmerica => Color.SandyBrown
    case Continent.SouthAmerica => Color.ForestGreen
    case Continent.Europe       => Color.LightBlue
    case Continent.Africa       => Color.Gold
    case Continent.Asia         => Color.Orange
    case Continent.Oceania      => Color.MediumPurple
    case Continent.Antarctica   => Color.White
    case Continent.Ocean        => Color.LightSteelBlue
  }

  def showWorldMap(): Unit = {
    val map = WorldMap.defaultMap
    val windowWidth = 1920.0
    val windowHeight = 1080.0

    // Hintergrundbild laden
    val bgImage = new Image(new FileInputStream("src/main/scala/de/htwg/codebreaker/assets/graphics/landscape.png"))
    val bgView = new ImageView(bgImage)
    bgView.preserveRatio = false
    bgView.fitWidth <== stage.width
    bgView.fitHeight <== stage.height

    // Pane für die Tiles
    val tilePane = new Pane {
      // Größe an Fenster binden
      prefWidth <== stage.width
      prefHeight <== stage.height
    }
    for (tile <- map.tiles) {
      val rect = new Rectangle {
        // Dynamische Bindung der Größe und Position
        width <== stage.width.divide(map.width)
        height <== stage.height.divide(map.height)
        x <== stage.width.divide(map.width).multiply(tile.x)
        y <== stage.height.divide(map.height).multiply(tile.y)
        fill = continentColor(tile.continent).deriveColor(0, 1, 1, 0.5)
        stroke = Color.Black
        strokeWidth = 0.5
      }
      rect.onMouseClicked = (_: MouseEvent) => {
        println(s"Tile clicked: (${tile.x}, ${tile.y}) - ${tile.continent}")
      }
      tilePane.children.add(rect)
    }

    val stack = new StackPane {
      children = Seq(bgView, tilePane)
    }

    stage.scene().root = new VBox {
      spacing = 0
      children = Seq(
        stack,
        new Button("Zurück") {
          onAction = _ => start()
        }
      )
    }
    stage.width = windowWidth
    stage.height = windowHeight
  }

  override def start(): Unit = {
    val showMapButton = new Button("Zeige Weltkarte")
    showMapButton.onAction = _ => showWorldMap()
    stage = new JFXApp3.PrimaryStage {
      title = "Codebreaker GUI"
      width = 1920
      height = 1080
      scene = new Scene {
        root = new VBox {
          spacing = 10
          children = Seq(
            new Label("Willkommen zu Codebreaker!"),
            showMapButton
          )
        }
      }
    }
  }
