package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import model.*

class WorldMapSpec extends AnyWordSpec with Matchers {

  val map = WorldMap.defaultMap

  "A WorldMap" should {

    "have correct dimensions" in {
      map.width shouldBe 20
      map.height shouldBe 10
    }

    "return tile at given coordinates" in {
      val tile = map.tileAt(2, 2)
      tile.isDefined shouldBe true
      tile.get.x shouldBe 2
      tile.get.y shouldBe 2
    }

    "return correct continent at a position" in {
      map.continentAt(2, 2) shouldBe Some(Continent.NorthAmerica)
      map.continentAt(7, 2) shouldBe Some(Continent.Europe)
      map.continentAt(8, 5) shouldBe Some(Continent.Africa)
    }

    "return all tiles for a continent" in {
      val asiaTiles = map.tilesOf(Continent.Asia)
      asiaTiles should not be empty
      all(asiaTiles.map(_.continent)) shouldBe Continent.Asia
    }

    "return None for tileAt with invalid coordinates" in {
      map.tileAt(-1, -1) shouldBe None
      map.tileAt(100, 100) shouldBe None
    }
  }
}