package de.htwg.codebreaker.view.gui

import com.google.inject.Inject
import de.htwg.codebreaker.controller.{ControllerInterface}
import de.htwg.codebreaker.controller.commands.{NextPlayerCommand}
import de.htwg.codebreaker.util.Observer
import de.htwg.codebreaker.view.gui.components._
import scalafx.application.{JFXApp3, Platform}
import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, BorderPane}
import de.htwg.codebreaker.view.gui.components.menu.pauseMenu.*
import scalafx.beans.property.{BooleanProperty, DoubleProperty}
import scala.compiletime.uninitialized
import de.htwg.codebreaker.model.server.Server

/**
 * Refaktorierte Graphical User Interface mit modularer Architektur.
 * Die GUI ist auf mehrere Komponenten aufgeteilt und vollständig skalierbar.
 * 
 * @param controller The game controller, injected by Guice
 */
class GUI @Inject() (val controller: ControllerInterface) extends JFXApp3 with Observer {
  
  controller.add(this)
  
  // Properties für Undo/Redo-Funktionalität
  var canUndoProperty: BooleanProperty = uninitialized
  var canRedoProperty: BooleanProperty = uninitialized
  
  // Properties für dynamische Fenstergröße
  private val windowWidthProperty: DoubleProperty = DoubleProperty(ViewConfig.DEFAULT_WIDTH)
  private val windowHeightProperty: DoubleProperty = DoubleProperty(ViewConfig.DEFAULT_HEIGHT)
  
  // Konfiguration für skalierbare Dimensionen
  private var config: ViewConfig = uninitialized
  
  // Komponenteninstanzen
  private var mapView: GameMapView = uninitialized
  private var playerSidebar: PlayerSidebar = uninitialized
  private var topBar: TopControlBar = uninitialized
  private val notificationHandler = new NotificationHandler()
  
  // Haupt-BorderPane für UI-Updates
  private var mainBorderPane: BorderPane = uninitialized
  
  // Tracking für Hack-Ereignisse
  private var previousServers: List[de.htwg.codebreaker.model.server.Server] = controller.getServers
  private var previousPlayers: List[de.htwg.codebreaker.model.player.Player] = controller.getPlayers
  
  // GUI-Modus
  enum GUIMode {
    case Menu, Game
  }
  private var mode: GUIMode = GUIMode.Menu
  
  /**
   * Initialisiert die Komponenten mit der aktuellen Konfiguration.
   */
  private def initializeComponents(): Unit = {
    config = new ViewConfig(windowWidthProperty, windowHeightProperty)
    mapView = new GameMapView(controller, config)
    playerSidebar = new PlayerSidebar(controller, config)
    topBar = new TopControlBar(
      controller,
      config,
      canUndoProperty,
      canRedoProperty,
      onPause = () => openPauseMenu()
    )
  }
  
  /**
   * Erstellt und zeigt die Hauptspielansicht an.
   */
  def showWorldMap(): Unit = {
    initializeComponents()
    
    mainBorderPane = new BorderPane {
      center = mapView.createMapPane()
      right = playerSidebar.createSidebar()
      top = topBar.createTopBar()
    }
    
    stage.scene = new Scene(windowWidthProperty.value, windowHeightProperty.value) {
      root = mainBorderPane
    }
    
    // Bind window size properties to actual stage dimensions
    windowWidthProperty <== stage.width
    windowHeightProperty <== stage.height
    
    // Listener für Fenstergrößen-Änderungen
    windowWidthProperty.onChange { (_, _, _) =>
      if (mode == GUIMode.Game) {
        updateGameView()
      }
    }
    
    windowHeightProperty.onChange { (_, _, _) =>
      if (mode == GUIMode.Game) {
        updateGameView()
      }
    }
  }
  
  /**
   * Aktualisiert nur die Spielansicht ohne komplette Neuinitialisierung.
   */
  private def updateGameView(): Unit = {
    Platform.runLater {
      // Komponenten mit neuer Config neu erstellen
      initializeComponents()
      
      // BorderPane-Inhalte aktualisieren
      mainBorderPane.center = mapView.createMapPane()
      mainBorderPane.right = playerSidebar.createSidebar()
      mainBorderPane.top = topBar.createTopBar()
    }
  }
  
  /**
   * Observer-Update: Reagiert auf Änderungen im Spielzustand.
   */
  override def update(): Unit = {
    val currentServers = controller.getServers
    val currentPlayers = controller.getPlayers
    
    // Erkennung und Anzeige von Hack-Ereignissen
    notificationHandler.detectAndShowHackResults(
      previousServers,
      currentServers,
      previousPlayers,
      currentPlayers
    )
    
    previousServers = currentServers
    previousPlayers = currentPlayers
    
    // UI-Updates auf JavaFX-Thread - OHNE Fenstergrößen-Updates
    Platform.runLater {
      if (mode == GUIMode.Game) {
        updateGameView()
      }
      canUndoProperty.value = controller.canUndo
      canRedoProperty.value = controller.canRedo
    }
  }
  
  /**
   * Startet das Spiel.
   */
  def startGame(): Unit = {
    canUndoProperty = BooleanProperty(controller.canUndo)
    canRedoProperty = BooleanProperty(controller.canRedo)
      mode = GUIMode.Game
    showWorldMap()
  }

  private def openPauseMenu(): Unit = {
    new PauseMenu(
        onResume = () => (),              // Spiel läuft weiter
        onSave   = () => controller.save(), // 💾 SaveGame
        onExit   = () => {
        Platform.exit()
        System.exit(0)
        }
    ).show()
  }
  
  /**
   * Haupteinstiegspunkt der Anwendung.
   */
  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "Codebreaker"
      width = ViewConfig.DEFAULT_WIDTH
      height = ViewConfig.DEFAULT_HEIGHT
      minWidth = ViewConfig.MIN_WIDTH
      minHeight = ViewConfig.MIN_HEIGHT
      resizable = true
      
      scene = new Scene {
        root = new VBox {
          spacing = 10
        }
      }
    }
    
    // Bind window dimensions
    windowWidthProperty.value = stage.width.value
    windowHeightProperty.value = stage.height.value
    
    // Starte das Spiel sofort
    startGame()
  }
}