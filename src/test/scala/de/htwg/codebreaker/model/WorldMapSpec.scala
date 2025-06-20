package de.htwg.codebreaker.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import MapObject._

class WorldMapSpec extends AnyWordSpec with Matchers {

  val map = WorldMap.defaultMap

  "WorldMap" should {

    "have correct dimensions and number of tiles" in {
      map.width shouldBe 80
      map.height shouldBe 40
      map.tiles.size shouldBe 3200
    }

    "return correct tile at coordinates" in {
      val tile = map.tileAt(10, 10)
      tile shouldBe defined
      tile.get.x shouldBe 10
      tile.get.y shouldBe 10
    }

    "return None for out-of-bound coordinates" in {
      map.tileAt(-1, -1) shouldBe None
      map.tileAt(1000, 1000) shouldBe None
    }

    "return continent at valid position" in {
      // Wähle eine bekannte NA-Position aus der northAmericaTiles-Liste
      map.continentAt(15, 14) shouldBe Some(Continent.NorthAmerica)
    }

    "return None for continent at invalid position" in {
      map.continentAt(-1, -1) shouldBe None
    }

    "return all tiles of a given continent" in {
      val asiaTiles = map.tilesOf(Continent.Asia)
      asiaTiles should not be empty
      all(asiaTiles.map(_.continent)) shouldBe Continent.Asia
    }

    "generate PlayerOnTile if only player is on tile" in {
      val tile = map.tileAt(2, 2).get
      val player = Player(0, "Solo", tile, 0, 0, 0, 0, 0, 0)
      val mapData = map.getMapData(List(player), Nil)
      mapData(tile.y)(tile.x) shouldBe PlayerOnTile(0)
    }

    "generate ServerOnTile if only server is on tile" in {
      val tile = map.tileAt(3, 2).get
      val server = Server("OnlyServer", tile, 10, 5, 5, false, ServerType.Firm)
      val mapData = map.getMapData(Nil, List(server))
      mapData(tile.y)(tile.x) shouldBe ServerOnTile(0, ServerType.Firm, tile.continent)
    }

    "generate PlayerAndServerTile if both are on same tile" in {
      val tile = map.tileAt(4, 4).get
      val player = Player(1, "Dual", tile, 0, 0, 0, 0, 0, 0)
      val server = Server("Together", tile, 0, 0, 0, false, ServerType.Private)
      val mapData = map.getMapData(List(player), List(server))
      mapData(tile.y)(tile.x) shouldBe PlayerAndServerTile(0, 0, ServerType.Private, tile.continent)
    }

    "generate EmptyTile when nothing is on tile" in {
      val tile = map.tileAt(0, 0).get
      val mapData = map.getMapData(Nil, Nil)
      mapData(tile.y)(tile.x) shouldBe EmptyTile(tile.continent)
    }

    "throw an exception if tile is missing" in {
      val modifiedMap = map.copy(tiles = map.tiles.filterNot(t => t.x == 79 && t.y == 39))
      an[IllegalArgumentException] should be thrownBy modifiedMap.getMapData(Nil, Nil)
    }

    "printContinentMap without crashing" in {
      noException should be thrownBy WorldMap.printContinentMap(map)
    }
  }
}
