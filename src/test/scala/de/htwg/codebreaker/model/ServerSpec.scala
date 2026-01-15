package de.htwg.codebreaker.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ServerSpec extends AnyWordSpec with Matchers {

  val tile = Tile(10, 3, Continent.Asia)

  "A Server" should {
    "store and return all properties" in {
      val server = Server("Mainframe", tile, 80, 20, 15, hacked = false, ServerType.Military)
      server.name shouldBe "Mainframe"
      server.tile shouldBe tile
      server.difficulty shouldBe 80
      server.rewardCpu shouldBe 20
      server.rewardRam shouldBe 15
      server.hacked shouldBe false
      server.serverType shouldBe ServerType.Military
      server.claimedBy shouldBe None
    }

    "be claimable and unclaimable using helper methods" in {
      val s       = Server("Bank", tile, 50, 20, 10, false, ServerType.Bank)
      val claimed = Server.claim(s, 0)
      claimed.claimedBy shouldBe Some(0)

      val unclaimed = Server.unclaim(claimed)
      unclaimed.claimedBy shouldBe None
    }

    "support copy for updates" in {
      val s      = Server("Node", tile, 40, 10, 5, false, ServerType.Firm)
      val hacked = s.copy(hacked = true)
      hacked.hacked shouldBe true
    }
  }
}
