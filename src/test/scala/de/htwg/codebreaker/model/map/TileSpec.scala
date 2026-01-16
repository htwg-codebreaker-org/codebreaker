package de.htwg.codebreaker.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TileSpec extends AnyWordSpec with Matchers:

  "Tile" should {

    "be created with coordinates and continent" in {
      val tile = Tile(5, 10, Continent.Europe)
      
      tile.x shouldBe 5
      tile.y shouldBe 10
      tile.continent shouldBe Continent.Europe
    }

    "support equality comparison" in {
      val tile1 = Tile(3, 4, Continent.Asia)
      val tile2 = Tile(3, 4, Continent.Asia)
      val tile3 = Tile(3, 5, Continent.Asia)
      
      tile1 shouldBe tile2
      tile1 should not be tile3
    }

    "create tiles for all continents" in {
      val tiles = Continent.values.map(c => Tile(0, 0, c))
      tiles should have size Continent.values.size
    }

    "allow different coordinates with same continent" in {
      val tile1 = Tile(1, 1, Continent.Africa)
      val tile2 = Tile(2, 2, Continent.Africa)
      
      tile1 should not be tile2
      tile1.continent shouldBe tile2.continent
    }

    "support copy with modifications" in {
      val original = Tile(10, 20, Continent.Ocean)
      val modified = original.copy(x = 11)
      
      modified.x shouldBe 11
      modified.y shouldBe 20
      modified.continent shouldBe Continent.Ocean
    }
  }
