package de.htwg.codebreaker.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlayerSpec extends AnyWordSpec with Matchers {

  val tile = Tile(5, 4, Continent.Europe)

  "A Player" should {
    "store all properties correctly" in {
      val player = Player(
        0,
        "Nico",
        tile,
        cpu = 50,
        ram = 20,
        code = 10,
        level = 1,
        xp = 0,
        cybersecurity = 30
      )
      player.id shouldBe 0
      player.name shouldBe "Nico"
      player.tile shouldBe tile
      player.cpu shouldBe 50
      player.cybersecurity shouldBe 30
    }

    "be updated via copy functionally" in {
      val p        = Player(1, "Nico", tile, 50, 20, 10, 1, 0, 0)
      val upgraded = p.copy(cpu = 100, xp = 500)
      upgraded.cpu shouldBe 100
      upgraded.xp shouldBe 500
    }

    "retain identity and name after copy" in {
      val p     = Player(5, "Nico", tile, 50, 20, 10, 1, 0, 0)
      val moved = p.copy(tile = Tile(6, 4, Continent.Europe))
      moved.name shouldBe "Nico"
      moved.id shouldBe 5
      moved.tile.x shouldBe 6
    }
  }
}
