// src/main/scala/de/htwg/codebreaker/model/WorldMap.scala
package de.htwg.codebreaker.model

import MapObject._

/**
 * Repräsentation der Weltkarte als Raster von Tiles.
 *
 * @param width   Breite in Tiles
 * @param height  Höhe in Tiles
 * @param tiles   Flache Liste aller Tiles mit Position und Kontinent
 */
case class WorldMap(width: Int, height: Int, tiles: Vector[Tile]):

  /**
   * Erzeugt eine 2D‑Matrix von MapObject, die für jede Position
   * angibt, ob dort Spieler, Server, beides oder nichts steht.
   *
   * Wirft IllegalArgumentException, falls auf ungültige Koordinaten
   * zugegriffen wird (tileAt(x,y).get).
   */
  def getMapData(players: List[Player], servers: List[Server]): Vector[Vector[MapObject]] =
    Vector.tabulate(height, width) { (y, x) =>
      tileAt(x, y) match
        case Some(tile) =>
          val maybePlayer = players.zipWithIndex.find((p, _) => p.tile == tile)
          val maybeServer = servers.zipWithIndex.find((s, _) => s.tile == tile)

          (maybePlayer, maybeServer) match
            case (Some((_, pIdx)), Some((server, sIdx))) =>
              // Beides auf derselben Kachel
              PlayerAndServerTile(pIdx, sIdx, server.serverType, tile.continent)
            case (Some((_, pIdx)), None) =>
              // Nur Spieler
              PlayerOnTile(pIdx)
            case (None, Some((server, sIdx))) =>
              // Nur Server
              ServerOnTile(sIdx, server.serverType, tile.continent)
            case (None, None) =>
              // Nichts
              EmptyTile(tile.continent)
        case None =>
          // Ungültige Koordinate
          throw new IllegalArgumentException(s"Invalid tile coordinates at ($x, $y)")
    }

  /** Liefert Some(Tile) für gültige Koordinaten, sonst None. */
  def tileAt(x: Int, y: Int): Option[Tile] =
    tiles.find(t => t.x == x && t.y == y)

  /** Alle Tiles eines bestimmten Kontinents. */
  def tilesOf(continent: Continent): Vector[Tile] =
    tiles.filter(_.continent == continent)

  /** Kontinent‑Lookup per Koordinate. */
  def continentAt(x: Int, y: Int): Option[Continent] =
    tileAt(x, y).map(_.continent)

object WorldMap:

  /** Erzeugt die Standard‑Map mit Breite=20, Höhe=10 und Kontinent‑Einteilung. */
  def defaultMap: WorldMap =
    val width  = 20
    val height = 10
    val tiles = for {
      y <- 0 until height
      x <- 0 until width
    } yield Tile(x, y, classifyContinent(x, y))
    WorldMap(width, height, tiles.toVector)

  /**
   * Ordnet jeder (x,y)‑Koordinate einen Kontinent zu.
   * Regeln basieren auf typischen Positionsbereichen.
   */
  private def classifyContinent(x: Int, y: Int): Continent =
    (x, y) match
      case (x, y) if x <= 4 && y <= 3                             => Continent.NorthAmerica
      case (x, y) if x >= 2 && x <= 4 && y >= 4 && y <= 7         => Continent.SouthAmerica
      case (x, y) if x >= 6 && x <= 8 && y >= 1 && y <= 3         => Continent.Europe
      case (x, y) if x >= 6 && x <= 8 && y >= 4 && y <= 6         => Continent.Africa
      case (x, y) if x >= 10 && x <= 17 && y <= 5                => Continent.Asia
      case (x, y) if x >= 17 && y >= 6 && y <= 8                  => Continent.Oceania
      case (_, 9)                                                 => Continent.Antarctica
      case _                                                      => Continent.Ocean

  /** Druckt die Kurz‑Codes (z.B. "NA", "EU", "~~") für alle Kacheln. */
  def printContinentMap(map: WorldMap): Unit =
    for y <- 0 until map.height do
      val row = for x <- 0 until map.width yield
        map.continentAt(x, y).map(_.short).getOrElse("--")
      println(row.mkString(" "))
