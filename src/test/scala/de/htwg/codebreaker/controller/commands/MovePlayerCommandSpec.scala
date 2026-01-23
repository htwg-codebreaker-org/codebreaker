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

    "fail with invalid player index (negative)" in {
      val game = baseGame
      val player = game.model.players.head

      val newTile = game.model.map.tiles.find { t =>
        t.continent.isLand && t != player.tile
      }.get

      val cmd = MovePlayerCommand(-1, newTile)
      val result = cmd.doStep(game)

      result.isFailure shouldBe true
    }

    "fail with invalid player index (too high)" in {
      val game = baseGame
      val player = game.model.players.head

      val newTile = game.model.map.tiles.find { t =>
        t.continent.isLand && t != player.tile
      }.get

      val cmd = MovePlayerCommand(99, newTile)
      val result = cmd.doStep(game)

      result.isFailure shouldBe true
    }

    "fail when trying to move to ocean tile" in {
      val game = baseGame
      val player = game.model.players.head

      val oceanTile = game.model.map.tiles.find { t =>
        !t.continent.isLand
      }.get

      val cmd = MovePlayerCommand(0, oceanTile)
      val result = cmd.doStep(game)

      result.isFailure shouldBe true
    }

    "fail when distance is 0 (same tile)" in {
      val game = baseGame
      val player = game.model.players.head

      val cmd = MovePlayerCommand(0, player.tile)
      val result = cmd.doStep(game)

      result.isFailure shouldBe true
    }

    "fail when not enough movement points" in {
      val game = baseGame
      val player = game.model.players.head

      // Find a tile that's too far away
      val farTile = game.model.map.tiles.find { t =>
        t.continent.isLand &&
        math.abs(t.x - player.tile.x) + math.abs(t.y - player.tile.y) > player.movementPoints
      }.get

      val cmd = MovePlayerCommand(0, farTile)
      val result = cmd.doStep(game)

      result.isFailure shouldBe true
    }

    "calculate correct movement cost based on Manhattan distance" in {
      val game = baseGame
      val player = game.model.players.head
      val initialMovement = player.movementPoints

      val newTile = game.model.map.tiles.find { t =>
        t.continent.isLand &&
        t != player.tile &&
        math.abs(t.x - player.tile.x) + math.abs(t.y - player.tile.y) <= player.movementPoints
      }.get

      val expectedDistance = math.abs(newTile.x - player.tile.x) + math.abs(newTile.y - player.tile.y)

      val cmd = MovePlayerCommand(0, newTile)
      val result = cmd.doStep(game).get

      val moved = result.model.players.head
      moved.movementPoints shouldBe (initialMovement - expectedDistance)
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

    "throw exception when undoing without previous state" in {
      val game = baseGame

      val cmd = MovePlayerCommand(0, game.model.players.head.tile)

      // Try to undo without calling doStep first
      val result = cmd.undoStep(game)
      result.isFailure shouldBe true
    }

  }
}
