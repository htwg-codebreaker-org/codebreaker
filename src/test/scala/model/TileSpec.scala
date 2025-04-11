package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TileSpec extends AnyWordSpec with Matchers {

  "A Tile" should {
    "store position and continent" in {
      val tile = Tile(5, 3, Continent.Asia)

      tile.x shouldBe 5
      tile.y shouldBe 3
      tile.continent shouldBe Continent.Asia
    }

    "correctly represent equality" in {
      val t1 = Tile(2, 4, Continent.Europe)
      val t2 = Tile(2, 4, Continent.Europe)

      t1 shouldEqual t2
    }
  }
}