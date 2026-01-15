package de.htwg.codebreaker.view.gui

import com.google.inject.Inject
import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.util.Observer
import de.htwg.codebreaker.model.{Continent, ServerType, WorldMap}
import de.htwg.codebreaker.controller.{HackServerCommand, Command, NextPlayerCommand, MovePlayerCommand}
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, Alert}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout.{VBox, GridPane, StackPane, Pane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Rectangle, Circle}
import scalafx.scene.input.MouseEvent
import scalafx.Includes._
import scala.compiletime.uninitialized
import scalafx.scene.image.Image
import scalafx.scene.image.ImageView
import java.io.FileInputStream
import scalafx.beans.binding.Bindings
import scalafx.beans.property.BooleanProperty
import scalafx.application.Platform

/**
 * Graphical User Interface component using ScalaFX.
 * Depends only on ControllerInterface, not on the concrete Controller implementation.
 * Implements Observer to receive updates when the game state changes.
 *
 * @param controller The game controller, injected by Guice
 */
class GUI @Inject() (val controller: ControllerInterface) extends JFXApp3 with Observer:


  controller.add(this)
  var canUndoProperty: BooleanProperty = uninitialized
  var canRedoProperty: BooleanProperty = uninitialized

  // Track previous server states to detect hack events
  private var previousServers: List[de.htwg.codebreaker.model.Server] = controller.getServers
  // Track previous player states to calculate rewards
  private var previousPlayers: List[de.htwg.codebreaker.model.Player] = controller.getPlayers



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
        // Direkte Bewegung zum angeklickten Tile (wenn Land)
        if (tile.continent.isLand) {
          val currentPlayerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
          controller.doAndRemember(MovePlayerCommand(currentPlayerIndex, tile))
        } else {
          println(s"Ocean-Tile bei (${tile.x}, ${tile.y}) - nicht begehbar")
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
      // Server-Icons lassen Maus-Events durch zum darunterliegenden Tile
      iconView.mouseTransparent = true
      tilePane.children.add(iconView)
    }

    // Player-Icons auf die Map legen
    val players = controller.getPlayers
    for ((player, index) <- players.zipWithIndex) {
      val playerIcon = new scalafx.scene.shape.Circle {
        radius <== stage.width.divide(map.width) * 0.3
        centerX <== stage.width.divide(map.width).multiply(player.tile.x) + stage.width.divide(map.width) * 0.5
        centerY <== stage.height.divide(map.height).multiply(player.tile.y) + stage.height.divide(map.height) * 0.5
        fill = if (index == 0) Color.Blue else Color.Red
        stroke = Color.White
        strokeWidth = 2
        mouseTransparent = true
      }
      val playerLabel = new Label(s"P$index") {
        layoutX <== stage.width.divide(map.width).multiply(player.tile.x) + stage.width.divide(map.width) * 0.35
        layoutY <== stage.height.divide(map.height).multiply(player.tile.y) + stage.height.divide(map.height) * 0.35
        style = "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;"
        mouseTransparent = true
      }
      tilePane.children.addAll(playerIcon, playerLabel)
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
        new Label(s"Bewegungspunkte: ${player.movementPoints}/${player.maxMovementPoints}") { style = "-fx-text-fill: #4db8ff; -fx-font-weight: bold;" },
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

    // Check for hack events before updating
    val currentServers = controller.getServers
    val currentPlayers = controller.getPlayers
    detectAndShowHackResult(previousServers, currentServers, previousPlayers, currentPlayers)
    previousServers = currentServers
    previousPlayers = currentPlayers

    // Ensure UI updates happen on JavaFX application thread
    Platform.runLater {
      if mode == GUIMode.Game then
        showWorldMap()
        if canUndoProperty != null then canUndoProperty.value = controller.canUndo
        if canRedoProperty != null then canRedoProperty.value = controller.canRedo
    }
  }

  /**
   * Detects if a server was hacked and shows a notification popup with rewards
   */
  private def detectAndShowHackResult(
    oldServers: List[de.htwg.codebreaker.model.Server],
    newServers: List[de.htwg.codebreaker.model.Server],
    oldPlayers: List[de.htwg.codebreaker.model.Player],
    newPlayers: List[de.htwg.codebreaker.model.Player]
  ): Unit = {
    // Find servers that changed from not-hacked to hacked
    val newlyHackedServers = newServers.filter { newServer =>
      newServer.hacked && oldServers.exists(oldServer =>
        oldServer.name == newServer.name && !oldServer.hacked
      )
    }

    // Show popup for each newly hacked server with detailed rewards
    newlyHackedServers.foreach { server =>
      // Find the player who hacked this server
      server.hackedBy.foreach { playerIndex =>
        val oldPlayer = oldPlayers.lift(playerIndex)
        val newPlayer = newPlayers.lift(playerIndex)

        (oldPlayer, newPlayer) match {
          case (Some(old), Some(current)) =>
            // Calculate the actual rewards received
            val cpuGained = current.cpu - old.cpu
            val ramGained = current.ram - old.ram
            val codeGained = current.code - old.code
            val xpGained = current.xp - old.xp

            // Build reward details string
            val rewardLines = List(
              if (cpuGained > 0) Some(s"+$cpuGained CPU") else None,
              if (ramGained > 0) Some(s"+$ramGained RAM") else None,
              if (codeGained > 0) Some(s"+$codeGained Code") else None,
              if (xpGained > 0) Some(s"+$xpGained XP") else None
            ).flatten

            val rewardText = if (rewardLines.nonEmpty) {
              rewardLines.mkString("\n")
            } else {
              "Keine Belohnungen"
            }

            Platform.runLater {
              val alert = new Alert(AlertType.Information) {
                title = "Server gehackt!"
                headerText = s"Server '${server.name}' erfolgreich gehackt!"
                contentText = s"""Server-Typ: ${server.serverType}
                                 |Schwierigkeit: ${server.difficulty}%
                                 |
                                 |Belohnungen erhalten:
                                 |$rewardText""".stripMargin
              }
              alert.showAndWait()
            }

          case _ =>
            // Fallback if player data not available
            Platform.runLater {
              val alert = new Alert(AlertType.Information) {
                title = "Server gehackt!"
                headerText = s"Server '${server.name}' erfolgreich gehackt!"
                contentText = s"""Server-Typ: ${server.serverType}
                                 |Schwierigkeit: ${server.difficulty}%
                                 |
                                 |Belohnungen erhalten!""".stripMargin
              }
              alert.showAndWait()
            }
        }
      }
    }
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
