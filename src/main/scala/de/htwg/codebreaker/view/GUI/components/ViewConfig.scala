package de.htwg.codebreaker.view.gui.components

import scalafx.beans.property.DoubleProperty

/**
 * Zentrale Konfiguration für skalierbare GUI-Dimensionen.
 * Alle Größenangaben werden relativ zur Fenstergröße berechnet.
 */
class ViewConfig(windowWidthProperty: DoubleProperty, windowHeightProperty: DoubleProperty) {
  
  // Basis-Dimensionen
  def windowWidth: Double = windowWidthProperty.value
  def windowHeight: Double = windowHeightProperty.value
  
  // Sidebar-Breite (relativ zur Fensterbreite)
  def sidebarWidth: Double = windowWidth * 0.13 // ~13% der Breite
  
  // Map-Dimensionen
  def mapWidth: Double = windowWidth - sidebarWidth
  def mapHeight: Double = windowHeight - topBarHeight
  
  // Top-Bar Höhe
  def topBarHeight: Double = windowHeight * 0.06 // ~6% der Höhe
  
  // Tile-Dimensionen (abhängig von Map-Größe)
  def tileWidth(mapTileCount: Int): Double = mapWidth / mapTileCount
  def tileHeight(mapTileCount: Int): Double = mapHeight / mapTileCount
  
  // Icon-Größen (relativ zur Tile-Größe)
  def serverIconScale: Double = 1.2
  def playerIconScale: Double = 0.7
  
  // Font-Größen (relativ zur Fensterhöhe)
  def fontSizeSmall: Double = windowHeight * 0.011 // ~11px bei 1080p
  def fontSizeMedium: Double = windowHeight * 0.013 // ~14px bei 1080p
  def fontSizeLarge: Double = windowHeight * 0.015 // ~16px bei 1080p
  
  // Spacing und Padding (relativ zur Fenstergröße)
  def spacing: Double = windowWidth * 0.005 // ~10px bei 1920px
  def padding: Double = windowWidth * 0.008 // ~15px bei 1920px
  
  // Stroke-Breite
  def tileStrokeWidth: Double = 0.5
  def playerStrokeWidth: Double = 2
}

object ViewConfig {
  val DEFAULT_WIDTH = 1920.0
  val DEFAULT_HEIGHT = 1080.0
  val MIN_WIDTH = 1280.0
  val MIN_HEIGHT = 720.0
}