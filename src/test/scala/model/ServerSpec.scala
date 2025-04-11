package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ServerSpec extends AnyWordSpec with Matchers {

  "A Server" should {
    "contain correct data and type" in {
      val server = Server(
        name = "Pentagon",
        position = (2, 2),
        difficulty = 85,
        rewardCpu = 30,
        rewardRam = 50,
        hacked = false,
        serverType = ServerType.Military
      )

      server.name shouldBe "Pentagon"
      server.position shouldBe (2, 2)
      server.difficulty shouldBe 85
      server.rewardCpu shouldBe 30
      server.rewardRam shouldBe 50
      server.hacked shouldBe false
      server.serverType shouldBe ServerType.Military
    }
  }

  "ServerType" should {
    "contain expected server types" in {
      ServerType.values should contain allOf (
        ServerType.Side,
        ServerType.Firm,
        ServerType.Cloud,
        ServerType.Bank,
        ServerType.Military,
        ServerType.GKS,
        ServerType.Private
      )
    }
  }
}