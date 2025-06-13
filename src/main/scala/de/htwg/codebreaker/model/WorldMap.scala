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

  /** Erzeugt die Standard‑Map mit Breite=80, Höhe=40 und Kontinent‑Einteilung. */
  def defaultMap: WorldMap =
    val width  = 80
    val height = 40
    val tiles = for {
      y <- 0 until height
      x <- 0 until width
    } yield Tile(x, y, classifyContinent(x, y))
    WorldMap(width, height, tiles.toVector)

  // --- Koordinaten für Südamerika als Set ---
  private val southAmericaTiles: Set[(Int, Int)] = Set(
    (25,31),(24,31),(23,31),(23,30),(24,30),(25,30),(25,29),(24,29),(24,28),(25,28),(26,28),(27,28),
    (28,27),(27,27),(26,27),(25,27),(24,27),(24,26),(25,26),(26,26),(27,26),(28,26),(30,25),(29,25),
    (28,25),(27,25),(26,25),(25,25),(24,25),(24,24),(23,24),(25,24),(26,24),(27,24),(28,24),(29,24),
    (30,24),(31,23),(30,23),(29,23),(28,23),(27,23),(26,23),(25,23),(24,23),(23,23),(22,23),(22,22),
    (23,22),(24,22),(25,22),(26,22),(27,22),(28,22),(29,22),(30,22),(31,22),
    (23,21),(24,21),(25,21),(26,21),(27,21),(28,21),
    (27,20),(26,20),(25,20),(24,20),(23,20),(24,19)
  )

  /**
   * Ordnet jeder (x,y)‑Koordinate einen Kontinent zu.
   * Bereiche sind für 80x40 Tiles und orientieren sich an deinen Markierungen.
   * Ergänze weitere Bereiche nach Bedarf!
   */
  private def classifyContinent(x: Int, y: Int): Continent =
    if (southAmericaTiles.contains((x, y))) Continent.SouthAmerica
    // --- Grönland (Europe): alle von dir markierten Tiles ---
    else if (
      (x == 28 && (y >= 6 && y <= 9)) ||
      (x == 29 && (y == 9)) ||
      (x == 30 && (y == 8 || y == 9)) ||
      (x == 31 && (y == 8)) ||
      (x == 32 && (y == 8)) ||
      (x == 33 && (y == 7)) ||
      (x == 34 && (y == 6))
    ) Continent.Europe
    // --- Rest wie bisher (grobe Bereiche, ggf. weiter anpassen) ---
    else (x, y) match
      case (x, y) if x <= 18 && y <= 13 => Continent.NorthAmerica
      case (x, y) if x >= 28 && x <= 38 && y >= 6 && y <= 13 => Continent.Europe
      case (x, y) if x >= 32 && x <= 42 && y >= 14 && y <= 25 => Continent.Africa
      case (x, y) if x >= 43 && x <= 70 && y >= 4 && y <= 18 => Continent.Asia
      case (x, y) if x >= 65 && x <= 78 && y >= 25 && y <= 36 => Continent.Oceania
      case (_, y) if y >= 37 => Continent.Antarctica
      case _ => Continent.Ocean

  /** Druckt die Kurz‑Codes (z.B. "NA", "EU", "~~") für alle Kacheln. */
  def printContinentMap(map: WorldMap): Unit =
    for y <- 0 until map.height do
      val row = for x <- 0 until map.width yield
        map.continentAt(x, y).map(_.short).getOrElse("--")
      println(row.mkString(" "))
