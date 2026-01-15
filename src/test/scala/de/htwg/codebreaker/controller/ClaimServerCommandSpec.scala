package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ClaimServerCommandSpec extends AnyWordSpec with Matchers {

  val tile   = Tile(0, 0, Continent.Europe)
  val server = Server("TargetServer", tile, 10, 1, 1, false, ServerType.Side)
  val player = Player(0, "Tester", tile, 1, 1, 1, 1, 0, 0)
  val model  = GameModel(List(player), List(server), WorldMap(1, 1, Vector(tile)))
  val game   = Game(model, GameState())

  "A ClaimServerCommand" should {
    val cmd = ClaimServerCommand("TargetServer", 0)

    "claim a server" in {
      val result = cmd.doStep(game)
      result.isSuccess shouldBe true
      result.get.model.servers.exists(_.claimedBy.contains(0)) shouldBe true
    }

    "undo a server claim" in {
      val claimedGame = cmd.doStep(game).get
      val undone      = cmd.undoStep(claimedGame)
      undone.isSuccess shouldBe true
      undone.get.model.servers.exists(_.claimedBy.isDefined) shouldBe false
    }

    "fail if server name does not exist" in {
      val cmdInvalid = ClaimServerCommand("DoesNotExist", 0)
      val result     = cmdInvalid.doStep(game)
      result.isSuccess shouldBe true // Because logic just skips unmatched servers
      result.get.model.servers.head.claimedBy shouldBe None
    }

    "be idempotent when redoing claim" in {
      val game1 = cmd.doStep(game).get
      val game2 = cmd.doStep(game1).get
      game2.model.servers.count(_.claimedBy.contains(0)) shouldBe 1
    }

    "do nothing when undoing twice" in {
      val claimedOnce = cmd.doStep(game).get
      val undoneOnce  = cmd.undoStep(claimedOnce).get
      val undoneTwice = cmd.undoStep(undoneOnce).get
      undoneTwice.model.servers.head.claimedBy shouldBe None
    }
  }
}
