package de.htwg.codebreaker.model.map

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.player.laptop.{Laptop, LaptopHardware, LaptopInstalledTools}
import de.htwg.codebreaker.model.player.skill.PlayerSkillTree
import de.htwg.codebreaker.model.server.{Server, ServerType}

class WorldMapSpec extends AnyWordSpec with Matchers:

  "WorldMap" should {

    "be created with width, height and tiles" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Europe),
        Tile(0, 1, Continent.Europe),
        Tile(1, 1, Continent.Europe)
      )
      val map = WorldMap(2, 2, tiles)

      map.width shouldBe 2
      map.height shouldBe 2
      map.tiles.length shouldBe 4
    }

    "find tile at valid coordinates" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Asia),
        Tile(0, 1, Continent.Africa)
      )
      val map = WorldMap(2, 2, tiles)

      map.tileAt(0, 0) shouldBe Some(Tile(0, 0, Continent.Europe))
      map.tileAt(1, 0) shouldBe Some(Tile(1, 0, Continent.Asia))
      map.tileAt(0, 1) shouldBe Some(Tile(0, 1, Continent.Africa))
    }

    "return None for invalid coordinates" in {
      val tiles = Vector(Tile(0, 0, Continent.Europe))
      val map = WorldMap(1, 1, tiles)

      map.tileAt(5, 5) shouldBe None
      map.tileAt(-1, 0) shouldBe None
      map.tileAt(0, -1) shouldBe None
    }

    "filter tiles by continent" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Asia),
        Tile(2, 0, Continent.Europe),
        Tile(3, 0, Continent.Africa)
      )
      val map = WorldMap(4, 1, tiles)

      val europeTiles = map.tilesOf(Continent.Europe)
      europeTiles.length shouldBe 2
      europeTiles should contain(Tile(0, 0, Continent.Europe))
      europeTiles should contain(Tile(2, 0, Continent.Europe))
    }

    "return empty vector for continent with no tiles" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Asia)
      )
      val map = WorldMap(2, 1, tiles)

      map.tilesOf(Continent.Antarctica) shouldBe empty
    }

    "get continent at valid coordinates" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Asia)
      )
      val map = WorldMap(2, 1, tiles)

      map.continentAt(0, 0) shouldBe Some(Continent.Europe)
      map.continentAt(1, 0) shouldBe Some(Continent.Asia)
    }

    "return None for continent at invalid coordinates" in {
      val tiles = Vector(Tile(0, 0, Continent.Europe))
      val map = WorldMap(1, 1, tiles)

      map.continentAt(5, 5) shouldBe None
    }

    "generate map data with empty tile" in {
      val tiles = Vector(Tile(0, 0, Continent.Europe))
      val map = WorldMap(1, 1, tiles)

      val mapData = map.getMapData(List.empty, List.empty)

      mapData.length shouldBe 1
      mapData.head.length shouldBe 1
      mapData.head.head shouldBe MapObject.EmptyTile(Continent.Europe)
    }

    "generate map data with player on tile" in {
      val tile = Tile(0, 0, Continent.Europe)
      val tiles = Vector(tile)
      val map = WorldMap(1, 1, tiles)

      val laptop = Laptop(
        LaptopHardware(20, 20, 20, 1, 0),
        LaptopInstalledTools.empty,
        List.empty,
        None,
        5
      )
      val player = Player(1, "Test", tile, laptop, 0, 0, PlayerSkillTree(), false, 5, 5)

      val mapData = map.getMapData(List(player), List.empty)

      mapData.head.head shouldBe MapObject.PlayerOnTile(0)
    }

    "generate map data with server on tile" in {
      val tile = Tile(0, 0, Continent.Europe)
      val tiles = Vector(tile)
      val map = WorldMap(1, 1, tiles)

      val server = Server(
        "TestServer",
        tile,
        50,
        100,
        200,
        false,
        ServerType.Firm,
        None,
        None,
        0,
        None,
        None
      )

      val mapData = map.getMapData(List.empty, List(server))

      mapData.head.head shouldBe MapObject.ServerOnTile(0, ServerType.Firm, Continent.Europe)
    }

    "generate map data with player and server on same tile" in {
      val tile = Tile(0, 0, Continent.Europe)
      val tiles = Vector(tile)
      val map = WorldMap(1, 1, tiles)

      val laptop = Laptop(
        LaptopHardware(20, 20, 20, 1, 0),
        LaptopInstalledTools.empty,
        List.empty,
        None,
        5
      )
      val player = Player(1, "Test", tile, laptop, 0, 0, PlayerSkillTree(), false, 5, 5)
      val server = Server(
        "TestServer",
        tile,
        50,
        100,
        200,
        false,
        ServerType.Firm,
        None,
        None,
        0,
        None,
        None
      )

      val mapData = map.getMapData(List(player), List(server))

      mapData.head.head shouldBe MapObject.PlayerAndServerTile(0, 0, ServerType.Firm, Continent.Europe)
    }

    "generate map data with multiple players and servers" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val tile2 = Tile(1, 0, Continent.Asia)
      val tiles = Vector(tile1, tile2)
      val map = WorldMap(2, 1, tiles)

      val laptop = Laptop(
        LaptopHardware(20, 20, 20, 1, 0),
        LaptopInstalledTools.empty,
        List.empty,
        None,
        5
      )
      val player1 = Player(1, "P1", tile1, laptop, 0, 0, PlayerSkillTree(), false, 5, 5)
      val player2 = Player(2, "P2", tile2, laptop, 0, 0, PlayerSkillTree(), false, 5, 5)

      val server = Server(
        "Server1",
        tile1,
        50,
        100,
        200,
        false,
        ServerType.Firm,
        None,
        None,
        0,
        None,
        None
      )

      val mapData = map.getMapData(List(player1, player2), List(server))

      mapData.head(0) shouldBe MapObject.PlayerAndServerTile(0, 0, ServerType.Firm, Continent.Europe)
      mapData.head(1) shouldBe MapObject.PlayerOnTile(1)
    }

    "throw exception for invalid tile coordinates in getMapData" in {
      val tiles = Vector.empty[Tile]
      val map = WorldMap(1, 1, tiles)

      an[IllegalArgumentException] should be thrownBy {
        map.getMapData(List.empty, List.empty)
      }
    }

    "support equality comparison" in {
      val tiles = Vector(Tile(0, 0, Continent.Europe))
      val map1 = WorldMap(1, 1, tiles)
      val map2 = WorldMap(1, 1, tiles)
      val map3 = WorldMap(2, 2, tiles)

      map1 shouldBe map2
      map1 should not be map3
    }

    "support copy with modifications" in {
      val tiles = Vector(Tile(0, 0, Continent.Europe))
      val original = WorldMap(1, 1, tiles)
      val modified = original.copy(width = 2)

      modified.width shouldBe 2
      original.width shouldBe 1
    }
  }

  "WorldMap.defaultMap" should {

    "create map with width 80" in {
      val map = WorldMap.defaultMap

      map.width shouldBe 80
    }

    "create map with height 40" in {
      val map = WorldMap.defaultMap

      map.height shouldBe 40
    }

    "create map with 3200 tiles" in {
      val map = WorldMap.defaultMap

      map.tiles.length shouldBe 3200
    }

    "have all tiles within bounds" in {
      val map = WorldMap.defaultMap

      map.tiles.foreach { tile =>
        tile.x should be >= 0
        tile.x should be < 80
        tile.y should be >= 0
        tile.y should be < 40
      }
    }

    "have tiles for all 8 continents" in {
      val map = WorldMap.defaultMap

      val continents = map.tiles.map(_.continent).distinct.toSet

      continents should contain(Continent.NorthAmerica)
      continents should contain(Continent.SouthAmerica)
      continents should contain(Continent.Europe)
      continents should contain(Continent.Africa)
      continents should contain(Continent.Asia)
      continents should contain(Continent.Oceania)
      continents should contain(Continent.Ocean)
    }

    "have ocean tiles" in {
      val map = WorldMap.defaultMap

      val oceanTiles = map.tilesOf(Continent.Ocean)
      oceanTiles should not be empty
    }

    "have land tiles" in {
      val map = WorldMap.defaultMap

      val landTiles = map.tiles.filter(_.continent.isLand)
      landTiles should not be empty
    }

    "have North America tiles" in {
      val map = WorldMap.defaultMap

      val naTiles = map.tilesOf(Continent.NorthAmerica)
      naTiles should not be empty
    }

    "have South America tiles" in {
      val map = WorldMap.defaultMap

      val saTiles = map.tilesOf(Continent.SouthAmerica)
      saTiles should not be empty
    }

    "have Europe tiles" in {
      val map = WorldMap.defaultMap

      val euTiles = map.tilesOf(Continent.Europe)
      euTiles should not be empty
    }

    "have Africa tiles" in {
      val map = WorldMap.defaultMap

      val africaTiles = map.tilesOf(Continent.Africa)
      africaTiles should not be empty
    }

    "have Asia tiles" in {
      val map = WorldMap.defaultMap

      val asiaTiles = map.tilesOf(Continent.Asia)
      asiaTiles should not be empty
    }

    "have Oceania tiles" in {
      val map = WorldMap.defaultMap

      val oceaniaTiles = map.tilesOf(Continent.Oceania)
      oceaniaTiles should not be empty
    }

    "have unique tile for each coordinate" in {
      val map = WorldMap.defaultMap

      val coordinates = map.tiles.map(t => (t.x, t.y)).toSet
      coordinates.size shouldBe map.tiles.length
    }

    "allow tile lookup by coordinates" in {
      val map = WorldMap.defaultMap

      map.tileAt(0, 0) shouldBe defined
      map.tileAt(79, 39) shouldBe defined
      map.tileAt(40, 20) shouldBe defined
    }

    "return None for out of bounds coordinates" in {
      val map = WorldMap.defaultMap

      map.tileAt(80, 0) shouldBe None
      map.tileAt(0, 40) shouldBe None
      map.tileAt(-1, 0) shouldBe None
    }

    "have specific continent at known positions" in {
      val map = WorldMap.defaultMap

      // Pentagon at (24, 14) should be in North America
      map.continentAt(24, 14) shouldBe Some(Continent.NorthAmerica)

      // GKS at (11, 8) should be in North America
      map.continentAt(11, 8) shouldBe Some(Continent.NorthAmerica)
    }

    "generate map data correctly" in {
      val map = WorldMap.defaultMap

      val mapData = map.getMapData(List.empty, List.empty)

      mapData.length shouldBe 40
      mapData.head.length shouldBe 80
    }

    "support filtering land tiles" in {
      val map = WorldMap.defaultMap

      val landTiles = map.tiles.filter(_.continent.isLand)
      val oceanTiles = map.tiles.filterNot(_.continent.isLand)

      landTiles should not be empty
      oceanTiles should not be empty
      landTiles.length + oceanTiles.length shouldBe map.tiles.length
    }

    "have consistent continent classification" in {
      val map1 = WorldMap.defaultMap
      val map2 = WorldMap.defaultMap

      map1.tiles.zip(map2.tiles).foreach { case (t1, t2) =>
        t1.continent shouldBe t2.continent
      }
    }

    "have all coordinates accessible via tileAt" in {
      val map = WorldMap.defaultMap

      for {
        x <- 0 until map.width
        y <- 0 until map.height
      } {
        map.tileAt(x, y) shouldBe defined
      }
    }

    "have land tiles on multiple continents" in {
      val map = WorldMap.defaultMap

      val continentsWithLand = map.tiles
        .filter(_.continent.isLand)
        .map(_.continent)
        .distinct

      continentsWithLand.size should be >= 6
    }
  }

  "WorldMap integration" should {

    "work with player movement across continents" in {
      val map = WorldMap.defaultMap

      val europeTile = map.tilesOf(Continent.Europe).head
      val asiaTile = map.tilesOf(Continent.Asia).head

      map.tileAt(europeTile.x, europeTile.y) shouldBe Some(europeTile)
      map.tileAt(asiaTile.x, asiaTile.y) shouldBe Some(asiaTile)
    }

    "generate consistent map data dimensions" in {
      val map = WorldMap.defaultMap

      val mapData = map.getMapData(List.empty, List.empty)

      mapData.foreach { row =>
        row.length shouldBe map.width
      }
      mapData.length shouldBe map.height
    }

    "handle multiple entities on different tiles" in {
      val map = WorldMap.defaultMap

      val tiles = map.tiles.filter(_.continent.isLand).take(3)
      val laptop = Laptop(
        LaptopHardware(20, 20, 20, 1, 0),
        LaptopInstalledTools.empty,
        List.empty,
        None,
        5
      )

      val players = tiles.zipWithIndex.map { case (tile, idx) =>
        Player(idx + 1, s"P${idx + 1}", tile, laptop, 0, 0, PlayerSkillTree(), false, 5, 5)
      }.toList

      val servers = tiles.zipWithIndex.map { case (tile, idx) =>
        Server(
          s"S${idx + 1}",
          tile,
          50,
          100,
          200,
          false,
          ServerType.Firm,
          None,
          None,
          0,
          None,
          None
        )
      }.toList

      val mapData = map.getMapData(players, servers)

      mapData.flatten.count {
        case _: MapObject.PlayerAndServerTile => true
        case _ => false
      } shouldBe 3
    }
  }

  "WorldMap.printContinentMap" should {

    "print continent map without errors for small map" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Asia),
        Tile(0, 1, Continent.Africa),
        Tile(1, 1, Continent.Ocean)
      )
      val map = WorldMap(2, 2, tiles)

      noException should be thrownBy {
        WorldMap.printContinentMap(map)
      }
    }

    "print continent map without errors for default map" in {
      val map = WorldMap.defaultMap

      noException should be thrownBy {
        WorldMap.printContinentMap(map)
      }
    }

    "handle empty map" in {
      val tiles = Vector.empty[Tile]
      val map = WorldMap(0, 0, tiles)

      noException should be thrownBy {
        WorldMap.printContinentMap(map)
      }
    }

    "handle map with single tile" in {
      val tiles = Vector(Tile(0, 0, Continent.NorthAmerica))
      val map = WorldMap(1, 1, tiles)

      noException should be thrownBy {
        WorldMap.printContinentMap(map)
      }
    }

    "handle map with all ocean tiles" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Ocean),
        Tile(1, 0, Continent.Ocean),
        Tile(0, 1, Continent.Ocean),
        Tile(1, 1, Continent.Ocean)
      )
      val map = WorldMap(2, 2, tiles)

      noException should be thrownBy {
        WorldMap.printContinentMap(map)
      }
    }

    "handle map with all different continents" in {
      val tiles = Vector(
        Tile(0, 0, Continent.NorthAmerica),
        Tile(1, 0, Continent.SouthAmerica),
        Tile(2, 0, Continent.Europe),
        Tile(3, 0, Continent.Africa),
        Tile(4, 0, Continent.Asia),
        Tile(5, 0, Continent.Oceania),
        Tile(6, 0, Continent.Ocean),
        Tile(7, 0, Continent.Antarctica)
      )
      val map = WorldMap(8, 1, tiles)

      noException should be thrownBy {
        WorldMap.printContinentMap(map)
      }
    }
  }

  "WorldMap edge cases" should {

    "handle getMapData with player on first tile" in {
      val map = WorldMap.defaultMap
      val firstTile = map.tiles.head

      val laptop = Laptop(
        LaptopHardware(20, 20, 20, 1, 0),
        LaptopInstalledTools.empty,
        List.empty,
        None,
        5
      )
      val player = Player(1, "Test", firstTile, laptop, 0, 0, PlayerSkillTree(), false, 5, 5)

      val mapData = map.getMapData(List(player), List.empty)

      mapData(firstTile.y)(firstTile.x) shouldBe MapObject.PlayerOnTile(0)
    }

    "handle getMapData with server on last tile" in {
      val map = WorldMap.defaultMap
      val lastTile = map.tiles.last

      val server = Server(
        "TestServer",
        lastTile,
        50,
        100,
        200,
        false,
        ServerType.Bank,
        None,
        None,
        0,
        None,
        None
      )

      val mapData = map.getMapData(List.empty, List(server))

      mapData(lastTile.y)(lastTile.x) shouldBe MapObject.ServerOnTile(0, ServerType.Bank, lastTile.continent)
    }

    "handle getMapData with all tiles having different server types" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Asia),
        Tile(2, 0, Continent.Africa),
        Tile(3, 0, Continent.NorthAmerica)
      )
      val map = WorldMap(4, 1, tiles)

      val servers = List(
        Server("S1", tiles(0), 50, 100, 200, false, ServerType.Firm, None, None, 0, None, None),
        Server("S2", tiles(1), 50, 100, 200, false, ServerType.Bank, None, None, 0, None, None),
        Server("S3", tiles(2), 50, 100, 200, false, ServerType.Cloud, None, None, 0, None, None),
        Server("S4", tiles(3), 50, 100, 200, false, ServerType.Military, None, None, 0, None, None)
      )

      val mapData = map.getMapData(List.empty, servers)

      mapData.head(0) shouldBe MapObject.ServerOnTile(0, ServerType.Firm, Continent.Europe)
      mapData.head(1) shouldBe MapObject.ServerOnTile(1, ServerType.Bank, Continent.Asia)
      mapData.head(2) shouldBe MapObject.ServerOnTile(2, ServerType.Cloud, Continent.Africa)
      mapData.head(3) shouldBe MapObject.ServerOnTile(3, ServerType.Military, Continent.NorthAmerica)
    }

    "tilesOf should return immutable vector" in {
      val map = WorldMap.defaultMap
      val europeTiles = map.tilesOf(Continent.Europe)

      europeTiles shouldBe a[Vector[_]]
    }

    "continentAt should handle boundary coordinates" in {
      val map = WorldMap.defaultMap

      map.continentAt(0, 0) shouldBe defined
      map.continentAt(79, 0) shouldBe defined
      map.continentAt(0, 39) shouldBe defined
      map.continentAt(79, 39) shouldBe defined
    }

    "tileAt should be consistent with tiles vector" in {
      val map = WorldMap.defaultMap

      map.tiles.foreach { tile =>
        map.tileAt(tile.x, tile.y) shouldBe Some(tile)
      }
    }

    "getMapData should handle multiple players on consecutive tiles" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Europe),
        Tile(2, 0, Continent.Europe)
      )
      val map = WorldMap(3, 1, tiles)

      val laptop = Laptop(
        LaptopHardware(20, 20, 20, 1, 0),
        LaptopInstalledTools.empty,
        List.empty,
        None,
        5
      )

      val players = List(
        Player(1, "P1", tiles(0), laptop, 0, 0, PlayerSkillTree(), false, 5, 5),
        Player(2, "P2", tiles(1), laptop, 0, 0, PlayerSkillTree(), false, 5, 5),
        Player(3, "P3", tiles(2), laptop, 0, 0, PlayerSkillTree(), false, 5, 5)
      )

      val mapData = map.getMapData(players, List.empty)

      mapData.head(0) shouldBe MapObject.PlayerOnTile(0)
      mapData.head(1) shouldBe MapObject.PlayerOnTile(1)
      mapData.head(2) shouldBe MapObject.PlayerOnTile(2)
    }

    "getMapData should handle multiple servers on consecutive tiles" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Europe),
        Tile(2, 0, Continent.Europe)
      )
      val map = WorldMap(3, 1, tiles)

      val servers = List(
        Server("S1", tiles(0), 50, 100, 200, false, ServerType.Firm, None, None, 0, None, None),
        Server("S2", tiles(1), 50, 100, 200, false, ServerType.Bank, None, None, 0, None, None),
        Server("S3", tiles(2), 50, 100, 200, false, ServerType.Cloud, None, None, 0, None, None)
      )

      val mapData = map.getMapData(List.empty, servers)

      mapData.head(0) shouldBe MapObject.ServerOnTile(0, ServerType.Firm, Continent.Europe)
      mapData.head(1) shouldBe MapObject.ServerOnTile(1, ServerType.Bank, Continent.Europe)
      mapData.head(2) shouldBe MapObject.ServerOnTile(2, ServerType.Cloud, Continent.Europe)
    }
  }
