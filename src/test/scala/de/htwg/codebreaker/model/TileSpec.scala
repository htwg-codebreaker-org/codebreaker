import de.htwg.codebreaker.model.{Tile, _}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TileSpec extends AnyWordSpec with Matchers {

  "A Tile" should {
    "store position and continent" in {
      val tile = Tile(3, 7, Continent.Africa)
      tile.x shouldBe 3
      tile.y shouldBe 7
      tile.continent shouldBe Continent.Africa
    }
  }
}
