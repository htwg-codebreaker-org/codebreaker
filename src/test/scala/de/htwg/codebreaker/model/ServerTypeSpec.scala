package de.htwg.codebreaker.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ServerTypeSpec extends AnyWordSpec with Matchers {

  "ServerType enum" should {
    "have all expected values" in {
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

    "have unique values" in {
      val allTypes = ServerType.values.toSet
      allTypes.size shouldBe ServerType.values.length
    }

    "support toString" in {
      ServerType.values.foreach { t =>
        t.toString should not be empty
      }
    }
  }
}
