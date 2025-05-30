package de.htwg.codebreaker.controller

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.util._
import de.htwg.codebreaker.model.game.strategy._

class ControllerSpec extends AnyWordSpec with Matchers {

  val game = GameFactory("default") // oder "default" falls du es so nennst
  val controller = Controller(game)


  class TestObserver extends Observer {
    var updated = false
    override def update: Unit = updated = true
  }

  "A Controller" should {

    "return all players and servers" in {
      controller.getPlayers should have size 2
      controller.getServers should not be empty
    }

    "claim a server and notify observers" in {
      val observer = new TestObserver
      controller.add(observer)

      val unclaimed = controller.getServers.find(_.claimedBy.isEmpty).get
      controller.claimServer(unclaimed.name, 0)

      val updated = controller.getServers.find(_.name == unclaimed.name).get
      updated.claimedBy shouldBe Some(0)
      observer.updated shouldBe true
    }

    "unclaim a server and notify observers" in {
      val observer = new TestObserver
      controller.add(observer)

      val anyServer = controller.getServers.find(_.claimedBy.isEmpty).get
      controller.claimServer(anyServer.name, 1)
      controller.unclaimServer(anyServer.name)

      val updated = controller.getServers.find(_.name == anyServer.name).get
      updated.claimedBy shouldBe None
      observer.updated shouldBe true
    }

    "return map data properly" in {
      val mapData = controller.getMapData()
      mapData shouldBe a [Vector[?]]
      mapData.flatten.exists(_.isInstanceOf[MapObject]) shouldBe true
    }
  }
}
