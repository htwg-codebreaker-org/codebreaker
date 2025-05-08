package de.htwg.codebreaker.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class ControllerSpec extends AnyWordSpec with Matchers {

  "A Controller" should {
    val controller = new Controller()

    "create a player" in {
      val player = controller.createPlayer("Alice")
      player.name should be ("Alice")
      player.id should be (1)
    }

    "set and get servers" in {
      val server = Server("TestServer", Tile(1,1,Continent.Europe), 50, 10, 10, false, ServerType.Firm)
      controller.setServers(List(server))
      controller.getServers should contain (server)
    }

    "claim a server" in {
      val server = Server("ClaimMe", Tile(2,2,Continent.Asia), 50, 10, 10, false, ServerType.Bank)
      controller.setServers(List(server))
      controller.claimServer("ClaimMe", 1)
      controller.getServers.exists(_.claimedBy.contains(1)) should be (true)
    }

    "unclaim a server" in {
      val server = Server("UnclaimMe", Tile(3,3,Continent.Africa), 50, 10, 10, false, ServerType.Military, Some(1))
      controller.setServers(List(server))
      controller.unclaimServer("UnclaimMe")
      controller.getServers.exists(_.claimedBy.isEmpty) should be (true)
    }

    "return map data" in {
      val mapData = controller.getMapData()
      mapData shouldBe a [Vector[?]]
    }
  }
}

