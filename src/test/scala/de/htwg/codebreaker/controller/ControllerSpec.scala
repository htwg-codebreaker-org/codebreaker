package de.htwg.codebreaker.controller

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.persistence.FileIOInterface
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.{Success, Try}

class ControllerSpec extends AnyWordSpec with Matchers {

  // Mock FileIO for testing (does nothing)
  val mockFileIO = new FileIOInterface {
    def save(game: Game): Try[Unit] = Success(())
    def load(): Try[Game]           = Success(game)
  }

  val tile       = Tile(0, 0, Continent.Europe)
  val player     = Player(0, "Test", tile, 1, 1, 1, 1, 0, 0)
  val server     = Server("S1", tile, 10, 2, 3, false, ServerType.Bank)
  val model      = GameModel(List(player), List(server), WorldMap(1, 1, Vector(tile)))
  val state      = GameState()
  val game       = Game(model, state)
  val controller = Controller(game, mockFileIO)

  "A Controller" should {

    "return correct players, servers and map" in {
      controller.getPlayers shouldBe List(player)
      controller.getServers shouldBe List(server)
      controller.getMapData().size shouldBe 1
    }

    "handle undo/redo stacks correctly" in {
      controller.canUndo shouldBe false
      controller.canRedo shouldBe false
    }

    "advance round" in {
      val oldRound = controller.getState.round
      controller.advanceRound()
      controller.getState.round shouldBe (oldRound + 1)
    }

    "change player and round via command" in {
      controller.doAndRemember(NextPlayerCommand())
      controller.getState.currentPlayerIndex shouldBe Some(0)
    }

    "set phase and status" in {
      controller.setPhase(Phase.ExecutingTurn)
      controller.getState.phase shouldBe Phase.ExecutingTurn

      controller.setStatus(GameStatus.Paused)
      controller.getState.status shouldBe GameStatus.Paused
    }

    "replace game with new one" in {
      val newGame = game.copy(state = GameState().copy(round = 99))
      controller.setGame(newGame)
      controller.getState.round shouldBe 99
    }
  }

  "doAndRemember should handle failing command" in {
    val failingCmd = new Command {
      def doStep(game: Game)   = scala.util.Failure(new RuntimeException("fail"))
      def undoStep(game: Game) = scala.util.Success(game)
    }
    noException should be thrownBy controller.doAndRemember(failingCmd)
  }

  "undo should print message if nothing to undo" in {
    val emptyController = Controller(game, mockFileIO)
    noException should be thrownBy emptyController.undo()
  }

  "redo should print message if nothing to redo" in {
    val emptyController = Controller(game, mockFileIO)
    noException should be thrownBy emptyController.redo()
  }

  "undo should handle failing undoStep" in {
    val cmd = new Command {
      def doStep(g: Game)   = scala.util.Success(g)
      def undoStep(g: Game) = scala.util.Failure(new RuntimeException("fail undo"))
    }
    controller.doAndRemember(cmd)
    noException should be thrownBy controller.undo()
  }

  "redo should handle failing doStep" in {
    val cmd = new Command {
      def doStep(g: Game)   = scala.util.Failure(new RuntimeException("redo fail"))
      def undoStep(g: Game) = scala.util.Success(g)
    }
    controller.doAndRemember(cmd)
    controller.undo()
    noException should be thrownBy controller.redo()
  }

}
