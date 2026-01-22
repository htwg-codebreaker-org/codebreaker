package de.htwg.codebreaker.controller.commands

import scala.util.Success

import de.htwg.codebreaker.model.map.Tile
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.controller.commands.player.MovePlayerCommand

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


class MovePlayerCommandSpec extends CommandTestBase {

  "MovePlayerCommand" should {

    "move a player to a new tile and reduce movement points" in {
      val game = baseGame
      val player = game.model.players.head
      val start = player.tile

      val newTile = game.model.map.tiles.find { t =>
        t.continent.isLand &&
        t != start &&
        math.abs(t.x - start.x) + math.abs(t.y - start.y) <= player.movementPoints
      }.get


      val cmd = MovePlayerCommand(0, newTile)
      val result = cmd.doStep(game).get

      val moved = result.model.players.head
      moved.tile shouldBe newTile
      moved.movementPoints should be < player.movementPoints
    }

    "undo a player move" in {
      val game = baseGame
      val player = game.model.players.head
      val start = player.tile

      val newTile = game.model.map.tiles.find { t =>
        t.continent.isLand &&
        t != start &&
        math.abs(t.x - start.x) + math.abs(t.y - start.y) <= player.movementPoints
      }.get

      val cmd = MovePlayerCommand(0, newTile)
      val afterDo = cmd.doStep(game).get
      val afterUndo = cmd.undoStep(afterDo).get

      val reverted = afterUndo.model.players.head
      reverted.tile shouldBe start
      reverted.movementPoints shouldBe player.movementPoints
    }

  }
}
