package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PlayerSpec extends AnyWordSpec with Matchers {

  "A Player" should {
    "store position and resources correctly" in {
      val player = Player("Alice", (1, 2), 50, 30, 10, 1, 0, 15)

      player.name shouldBe "Alice"
      player.position shouldBe (1, 2)
      player.cpu shouldBe 50
      player.ram shouldBe 30
      player.code shouldBe 10
      player.level shouldBe 1
      player.xp shouldBe 0
      player.cybersecurity shouldBe 15
    }

    "be upgradable via copy" in {
      val original = Player("Bob", (2, 3), 40, 20, 5, 1, 100, 10)
      val upgraded = original.copy(level = 2, xp = 150)

      upgraded.name shouldBe "Bob"
      upgraded.level shouldBe 2
      upgraded.xp shouldBe 150
      upgraded.position shouldBe (2, 3)
    }
  }
}