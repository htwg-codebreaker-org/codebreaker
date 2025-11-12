package de.htwg.codebreaker.view.gui

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.util.Observer
import de.htwg.codebreaker.model.{Continent, ServerType, WorldMap}
import de.htwg.codebreaker.controller.{ClaimServerCommand, Command, NextPlayerCommand}
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
import scalafx.beans.property.BooleanProperty

/**
 * Graphical User Interface component using ScalaFX.
 * Depends only on ControllerInterface, not on the concrete Controller implementation.
 * Implements Observer to receive updates when the game state changes.
 */
class GUI(val controller: ControllerInterface) extends JFXApp3 with Observer:


  controller.add(this)
  var canUndoProperty: BooleanProperty = uninitialized
  var canRedoProperty: BooleanProperty = uninitialized



  // State-Pattern-Vorbereitung: GUI-Modus
  enum GUIMode { case Menu, Game }
  private var mode: GUIMode = GUIMode.Menu

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
          val claimButton = new Button("Server claimen") {
            onAction = _ => {
              val currentPlayerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
              controller.doAndRemember(ClaimServerCommand(server.name, currentPlayerIndex))
            }
          }
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
                new Label(s"Besitzer: ${server.claimedBy.getOrElse("-")}"),
                claimButton
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

    // --- Nur aktiver Spieler ---
    val currentPlayerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
    val player = controller.getPlayers(currentPlayerIndex)
    val playerInfoBox = new VBox {
      style = "-fx-background-color: #222; -fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #888; -fx-border-width: 2;"
      spacing = 6
      children = Seq(
        new Label(s"Spieler: ${player.name}") { style = "-fx-text-fill: white; -fx-font-weight: bold;" },
        new Label(s"CPU: ${player.cpu}   RAM: ${player.ram}   Code: ${player.code}") { style = "-fx-text-fill: #ccc;" },
        new Label(s"Level: ${player.level}   XP: ${player.xp}") { style = "-fx-text-fill: #ccc;" },
        new Label(s"Cybersecurity: ${player.cybersecurity}") { style = "-fx-text-fill: #ccc;" },
        new Button("Undo") {
          disable = false
          onAction = _ => controller.undo()
          style = "-fx-background-color: #ffb84d; -fx-font-weight: bold;"
        }
      )
    }

    val playerInfoBar = new scalafx.scene.layout.HBox {
      style = "-fx-background-color: #222; -fx-padding: 10;"
      spacing = 20
      children = Seq(playerInfoBox)
    }

    val stack = new StackPane {
      children = Seq(bgView, tilePane)
    }

    // --- Top-Bar mit Status und Steuerung ---
    val topBar = new scalafx.scene.layout.HBox {
      style = "-fx-background-color: #222; -fx-padding: 10; -fx-spacing: 20; -fx-alignment: center-left;"
      children = Seq(
        new Label(s"Runde: ${controller.getState.round}") {
          style = "-fx-text-fill: #fff; -fx-font-size: 16; -fx-font-weight: bold;"
        },
        new Label(s"Am Zug: ${controller.getPlayers(controller.getState.currentPlayerIndex.getOrElse(0)).name}") {
          style = "-fx-text-fill: #4db8ff; -fx-font-size: 16; -fx-font-weight: bold;"
        },
        new scalafx.scene.layout.Region { hgrow = scalafx.scene.layout.Priority.Always },
        new Button("Rückgängig") {
          onAction = _ => controller.undo()
          disable <== canUndoProperty.not()
          style = "-fx-background-color: #ffcc00; -fx-text-fill: black; -fx-font-weight: bold;"
        },
        new Button("Wiederholen") {
          onAction = _ => controller.redo()
          disable <== canRedoProperty.not()
          style = "-fx-background-color: #66ff66; -fx-text-fill: black; -fx-font-weight: bold;"
        },
        new Button("Runde an nächsten Spieler") {
          onAction = _ => controller.doAndRemember(NextPlayerCommand())
          style = "-fx-background-color: #4db8ff; -fx-text-fill: white; -fx-font-weight: bold;"
        },
        new Button("Spiel beenden") {
          onAction = _ => start()
          style = "-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;"
        }
      )
    }


    val borderPane = new scalafx.scene.layout.BorderPane {
      center = stack
      bottom = playerInfoBar
      top = topBar
    }
    stage.scene = new Scene(windowWidth, windowHeight) {
      root = borderPane
    }
    stage.width = windowWidth
    stage.height = windowHeight
  }

  // --- Observer-Mechanismus für automatische GUI-Aktualisierung ---
  override def update(): Unit = {
    // Hier kann die GUI auf Änderungen im Spielzustand reagieren
    // Zum Beispiel: this.showWorldMap() oder Aktualisierung von Labels, etc.
    if mode == GUIMode.Game then showWorldMap()
    canUndoProperty.value = controller.canUndo
    canRedoProperty.value = controller.canRedo
  }

  def startGame(): Unit = {
    canUndoProperty = BooleanProperty(controller.canUndo)
    canRedoProperty = BooleanProperty(controller.canRedo)
    mode = GUIMode.Game
    println(s"[DEBUG] Aktueller Spieler bei Start: ${controller.getState.currentPlayerIndex}")
    if controller.getState.currentPlayerIndex.isEmpty then
      controller.doAndRemember(NextPlayerCommand())
    showWorldMap()
  }



  override def start(): Unit = {
    mode = GUIMode.Menu
    val startGameButton = new Button("Spiel starten")
    startGameButton.onAction = _ => startGame()
    stage = new JFXApp3.PrimaryStage {
      title = "Codebreaker GUI"
      width = 1920
      height = 1080
      scene = new Scene {
        root = new VBox {
          spacing = 10
          children = Seq(
            new Label("Willkommen zu Codebreaker!"),
            startGameButton
          )
        }
      }
    }
  }
