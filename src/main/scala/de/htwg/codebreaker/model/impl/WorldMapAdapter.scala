package de.htwg.codebreaker.model.impl

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.api._

class WorldMapAdapter(map: WorldMap) extends IWorldMap:
  override def width: Int = map.width
  override def height: Int = map.height
  override def tiles: Vector[ITile] = map.tiles.map(TileAdapter(_))

  override def getMapData(players: List[IPlayer], servers: List[IServer]): Vector[Vector[MapObject]] =
    // Achtung: Diese Methode braucht echten Zugriff auf `Player` und `Server`
    // Das funktioniert nicht direkt mit Interfaces, da man auf `tile` zugreifen und vergleichen muss.
    // Deshalb eventuell die Methode hier *nicht adaptieren* oder `MapObject` neu denken.
    throw new UnsupportedOperationException("getMapData nicht sinnvoll adaptierbar über Interfaces")

  override def tileAt(x: Int, y: Int): Option[ITile] =
    map.tileAt(x, y).map(TileAdapter(_))

  override def tilesOf(continent: Continent): Vector[ITile] =
    map.tilesOf(continent).map(TileAdapter(_))

  override def continentAt(x: Int, y: Int): Option[Continent] =
    map.continentAt(x, y)
