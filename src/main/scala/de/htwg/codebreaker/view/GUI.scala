package de.htwg.codebreaker.view

import de.htwg.codebreaker.controller.Controller
import de.htwg.codebreaker.model.{WorldMap, Continent, ServerType}
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
    case Continent.Europe       => Color.DarkBlue
    case Continent.Africa       => Color.Gold
    case Continent.Asia         => Color.Orange
    case Continent.Oceania      => Color.MediumPurple
    case Continent.Antarctica   => Color.White
    case Continent.Ocean        => Color.LightSteelBlue
  }

  def serverIconFile(serverType: ServerType): String = serverType match {
    case ServerType.Side     => "src/main/scala/de/htwg/codebreaker/assets/graphics/server/icons/side_server.png"
    case ServerType.Firm     => "src/main/scala/de/htwg/codebreaker/assets/graphics/server/icons/firm_server.png"
    case ServerType.Cloud    => "src/main/scala/de/htwg/codebreaker/assets/graphics/server/icons/cloud_server.png"
    case ServerType.Bank     => "src/main/scala/de/htwg/codebreaker/assets/graphics/server/icons/bank_server.png"
    case ServerType.Military => "src/main/scala/de/htwg/codebreaker/assets/graphics/server/icons/military_server.png"
    case ServerType.GKS      => "src/main/scala/de/htwg/codebreaker/assets/graphics/server/icons/gks_server.png"
    case ServerType.Private  => "src/main/scala/de/htwg/codebreaker/assets/graphics/server/icons/player_base.png"
  }

  def showWorldMap(): Unit = {
    val map = WorldMap.defaultMap
    val windowWidth = 1920.0
    val windowHeight = 1080.0

    val bgImage = new Image(new FileInputStream("src/main/scala/de/htwg/codebreaker/assets/graphics/landscape.png"))
    val bgView = new ImageView(bgImage)
    bgView.preserveRatio = false
    bgView.fitWidth <== stage.width
    bgView.fitHeight <== stage.height

    val tilePane = new Pane {
      prefWidth <== stage.width
      prefHeight <== stage.height
    }
    for (tile <- map.tiles) {
      val rect = new Rectangle {
        width <== stage.width.divide(map.width)
        height <== stage.height.divide(map.height)
        x <== stage.width.divide(map.width).multiply(tile.x)
        y <== stage.height.divide(map.height).multiply(tile.y)
        fill = continentColor(tile.continent).deriveColor(0, 1, 1, 0.5)
        stroke = Color.Black
        strokeWidth = 0.5
      }
      rect.onMouseClicked = (_: MouseEvent) => {
        val serverOpt = controller.getServers.find(_.tile == tile)
        if (serverOpt.isDefined) {
          val server = serverOpt.get
          // Schickes Info-Dialogfenster mit Serverdaten
          val dialog = new scalafx.scene.control.Dialog[Unit]() {
            title = s"Server: ${server.name}"
            headerText = s"Typ: ${server.serverType} | Schwierigkeit: ${server.difficulty} | Kontinent: ${tile.continent}"
            dialogPane().content = new VBox {
              spacing = 10
              children = Seq(
                new ImageView(new Image(new FileInputStream(serverIconFile(server.serverType)))) {
                  fitWidth = 80
                  fitHeight = 80
                  preserveRatio = true
                },
                new Label(s"Name: ${server.name}"),
                new Label(s"Belohnung CPU: ${server.rewardCpu}"),
                new Label(s"Belohnung RAM: ${server.rewardRam}"),
                new Label(s"Gehackt: ${if (server.hacked) "Ja" else "Nein"}"),
                new Label(s"Besitzer: ${server.claimedBy.getOrElse("-")}")
              )
            }
            dialogPane().buttonTypes = Seq(scalafx.scene.control.ButtonType.Close)
          }
          dialog.showAndWait()
        } else {
          println(s"Tile clicked: (${tile.x}, ${tile.y}) - ${tile.continent}")
        }
      }
      tilePane.children.add(rect)
    }

    // Server-Icons auf die Map legen
    val servers = controller.getServers
    for (server <- servers) {
      val iconPath = serverIconFile(server.serverType)
      val iconImg = new Image(new FileInputStream(iconPath))
      val iconView = new ImageView(iconImg)
      // Dynamische Größe: z.B. 60% der Tile-Kantenlänge
      iconView.fitWidth <== stage.width.divide(map.width) * 0.6
      iconView.fitHeight <== stage.height.divide(map.height) * 0.6
      iconView.preserveRatio = true
      // Position mittig auf Tile
      iconView.layoutX <== stage.width.divide(map.width).multiply(server.tile.x) + stage.width.divide(map.width) * 0.2
      iconView.layoutY <== stage.height.divide(map.height).multiply(server.tile.y) + stage.height.divide(map.height) * 0.2
      iconView.onMouseClicked = (_: MouseEvent) => {
        println(s"Server clicked: ${server.name} (${server.serverType}) auf Tile (${server.tile.x},${server.tile.y})")
        // Hier ggf. weitere Interaktion/Popup
      }
      tilePane.children.add(iconView)
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
