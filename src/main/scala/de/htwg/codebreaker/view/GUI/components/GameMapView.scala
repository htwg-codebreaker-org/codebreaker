package de.htwg.codebreaker.view.gui.components

import de.htwg.codebreaker.controller.ControllerInterface
import de.htwg.codebreaker.model.map.{WorldMap, Continent}
import de.htwg.codebreaker.model.server.ServerType
import de.htwg.codebreaker.view.gui.components.menu.playerActionMenu.TileActionMenu
import scalafx.scene.layout.{StackPane, Pane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Rectangle, Circle}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.control.Label
import scalafx.scene.input.{MouseEvent, MouseButton}
import scalafx.scene.effect.DropShadow
import scalafx.Includes._
import java.io.FileInputStream
import scala.compiletime.uninitialized

class GameMapView(
  controller: ControllerInterface,
  config: ViewConfig
) {
  
  private val ASSET_PATH = "src/main/scala/de/htwg/codebreaker/assets/graphics"
  
  // ⚡ Speichere Referenzen für refresh()
  private var mapStackPane: StackPane = uninitialized
  private var tilePane: Pane = uninitialized
  
  /**
   * Erstellt die Map-Pane initial.
   */
  def createMapPane(): StackPane = {
    val map = WorldMap.defaultMap
    
    // Hintergrundbild
    val bgImage = new Image(new FileInputStream(s"$ASSET_PATH/landscape.png"))
    val bgView = new ImageView(bgImage) {
      preserveRatio = false
      fitWidth = config.mapWidth
      fitHeight = config.mapHeight
    }
    
    // Tile-Layer erstellen
    tilePane = createTileLayer(map)
    
    // Server + Player hinzufügen
    addServerIcons(tilePane, map)
    addPlayerIcons(tilePane, map)
    
    mapStackPane = new StackPane {
      children = Seq(bgView, tilePane)
    }
    
    mapStackPane
  }
  
  /**
   * ⚡ REFRESH: Nur Inhalt aktualisieren, nicht neu erstellen!
   */
  def refresh(): Unit = {
    if (tilePane != null) {
      val map = WorldMap.defaultMap
      
      // Tile-Layer komplett neu zeichnen
      tilePane.children.clear()
      
      // Tiles neu zeichnen
      for (tile <- map.tiles) {
        val tileW = config.tileWidth(map.width)
        val tileH = config.tileHeight(map.height)
        
        val rect = new Rectangle {
          width = tileW
          height = tileH
          x = tileW * tile.x
          y = tileH * tile.y
          fill = continentColor(tile.continent).deriveColor(0, 1, 1, 0.5)
          stroke = Color.Black
          strokeWidth = config.tileStrokeWidth
        }
        
        rect.onMouseClicked = (event: MouseEvent) => {
          if (event.button == MouseButton.Primary) {
            val currentPlayerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
            val actionMenu = new TileActionMenu(controller, tile, currentPlayerIndex)
            val menu = actionMenu.create()
            menu.show(rect, event.screenX, event.screenY)
          }
        }
        
        tilePane.children.add(rect)
      }
      
      // Server + Player neu zeichnen
      addServerIcons(tilePane, map)
      addPlayerIcons(tilePane, map)
    }
  }
  
  private def createTileLayer(map: WorldMap): Pane = {
    val pane = new Pane {
      prefWidth = config.mapWidth
      prefHeight = config.mapHeight
    }
    
    for (tile <- map.tiles) {
      val tileW = config.tileWidth(map.width)
      val tileH = config.tileHeight(map.height)
      
      val rect = new Rectangle {
        width = tileW
        height = tileH
        x = tileW * tile.x
        y = tileH * tile.y
        fill = continentColor(tile.continent).deriveColor(0, 1, 1, 0.5)
        stroke = Color.Black
        strokeWidth = config.tileStrokeWidth
      }
      
      rect.onMouseClicked = (event: MouseEvent) => {
        if (event.button == MouseButton.Primary) {
          val currentPlayerIndex = controller.getState.currentPlayerIndex.getOrElse(0)
          val actionMenu = new TileActionMenu(controller, tile, currentPlayerIndex)
          val menu = actionMenu.create()
          menu.show(rect, event.screenX, event.screenY)
        }
      }
      
      pane.children.add(rect)
    }
    
    pane
  }
  
  private def addServerIcons(pane: Pane, map: WorldMap): Unit = {
    val servers = controller.getServers
    val tileW = config.tileWidth(map.width)
    val tileH = config.tileHeight(map.height)
    
    for (server <- servers) {
      val iconPath = serverIconFile(server.serverType)
      val iconImg = new Image(new FileInputStream(iconPath))
      val iconW = tileW * config.serverIconScale
      val iconH = tileH * config.serverIconScale

      val iconView = new ImageView(iconImg) {
        fitWidth = iconW
        fitHeight = iconH
        preserveRatio = true
        layoutX = tileW * server.tile.x + tileW * 0.5 - iconW * 0.5
        layoutY = tileH * server.tile.y + tileH * 0.5 - iconH * 0.5
        mouseTransparent = true
      }
      
      pane.children.add(iconView)
    }
  }
  
  private def addPlayerIcons(pane: Pane, map: WorldMap): Unit = {
    val players = controller.getPlayers
    val tileW = config.tileWidth(map.width)
    val tileH = config.tileHeight(map.height)
    
    for ((player, index) <- players.zipWithIndex) {
      val isActive = index == controller.getState.currentPlayerIndex.getOrElse(0)
      
      val playerIcon = new Circle {
        radius = tileW * config.playerIconScale
        centerX = tileW * player.tile.x + tileW * 0.5
        centerY = tileH * player.tile.y + tileH * 0.5
        fill = if (index == 0) Color.Blue else Color.Red
        stroke = if (isActive) Color.Yellow else Color.White
        strokeWidth = if (isActive) 4 else config.playerStrokeWidth
        mouseTransparent = true
      }

      if (isActive) {
        playerIcon.effect = new DropShadow {
          radius = 20
          color = Color.Gold
        }
      }
      
      val playerLabel = new Label(s"P$index") {
        layoutX = tileW * player.tile.x + tileW * 0.35
        layoutY = tileH * player.tile.y + tileH * 0.35
        style = s"-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: ${config.fontSizeSmall}px;"
        mouseTransparent = true
      }
      
      pane.children.addAll(playerIcon, playerLabel)
    }
  }
  
  private def continentColor(continent: Continent): Color = continent match {
    case Continent.NorthAmerica => Color.SandyBrown
    case Continent.SouthAmerica => Color.ForestGreen
    case Continent.Europe => Color.DarkBlue
    case Continent.Africa => Color.Gold
    case Continent.Asia => Color.Orange
    case Continent.Oceania => Color.MediumPurple
    case Continent.Antarctica => Color.White
    case Continent.Ocean => Color.LightSteelBlue
  }
  
  private def serverIconFile(serverType: ServerType): String = {
    val iconName = serverType match {
      case ServerType.Side => "side_server.png"
      case ServerType.Firm => "firm_server.png"
      case ServerType.Cloud => "cloud_server.png"
      case ServerType.Bank => "bank_server.png"
      case ServerType.Military => "military_server.png"
      case ServerType.GKS => "gks_server.png"
      case ServerType.Private => "player_base.png"
    }
    s"$ASSET_PATH/server/icons/$iconName"
  }
}