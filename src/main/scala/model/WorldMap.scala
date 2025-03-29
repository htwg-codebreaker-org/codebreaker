package model

case class WorldMap(width: Int, height: Int, tiles: Vector[Tile]):

  def display(players: List[Player], servers: List[Server]): String =
    val indexedServers = servers.zipWithIndex

    val output = for y <- 0 until height yield
      val row = for x <- 0 until width yield
        val pos = (x, y)

        players.zipWithIndex.find(_._1.position == pos) match
          case Some((_, pIndex)) => f"[P$pIndex]"
          case None =>
            indexedServers.find(_._1.position == pos) match
              case Some((server, idx)) =>
                val cont = continentAt(x, y).map(_.short).getOrElse("??")
                f"[$idx%2d]S-$cont"
              case None => " .   "
      row.mkString(" ")
    output.mkString("\n")


  def tileAt(x: Int, y: Int): Option[Tile] =
    tiles.find(t => t.x == x && t.y == y)

  def tilesOf(continent: Continent): Vector[Tile] =
    tiles.filter(_.continent == continent)


  def continentAt(x: Int, y: Int): Option[Continent] =
    tileAt(x, y).map(_.continent)

object WorldMap:
  
  def defaultMap: WorldMap =
    val width = 20
    val height = 10
    val tiles = for {
      y <- 0 until height
      x <- 0 until width
    } yield Tile(x, y, classifyContinent(x, y))

    WorldMap(width, height, tiles.toVector)


  private def classifyContinent(x: Int, y: Int): Continent =
    (x, y) match
      case (x, y) if x <= 4 && y <= 3 => Continent.NorthAmerica
      case (x, y) if x >= 2 && x <= 4 && y >= 4 && y <= 7 => Continent.SouthAmerica
      case (x, y) if x >= 6 && x <= 8 && y >= 1 && y <= 3 => Continent.Europe
      case (x, y) if x >= 6 && x <= 8 && y >= 4 && y <= 6 => Continent.Africa
      case (x, y) if x >= 10 && x <= 17 && y <= 5 => Continent.Asia
      case (x, y) if x >= 17 && y >= 6 && y <= 8 => Continent.Oceania
      case (_, 9) => Continent.Antarctica
      case _ => Continent.Ocean


  def printContinentMap(map: WorldMap): Unit =
    for y <- 0 until map.height do
      val row = for x <- 0 until map.width yield
        map.continentAt(x, y).map(_.short).getOrElse("--")
      println(row.mkString(" "))



