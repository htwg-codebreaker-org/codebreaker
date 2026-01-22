package de.htwg.codebreaker.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.util.{Success, Failure, Try}
import de.htwg.codebreaker.model.game._

class CommandInterfaceSpec extends AnyWordSpec with Matchers:

  "Command trait" should {

    "define doStep method" in {
      val game = TestGameFactory.game()
      val command = new Command {
        override def doStep(game: Game): Try[Game] = 
          Success(game.copy(state = game.state.advanceRound()))
        override def undoStep(game: Game): Try[Game] = 
          Success(game)
      }

      command.doStep(game) shouldBe a[Success[?]]
      command.doStep(game).get.state.round shouldBe 2
    }

    "define undoStep method" in {
      val game = TestGameFactory.game().copy(state = GameState().advanceRound())
      val command = new Command {
        override def doStep(game: Game): Try[Game] =
          Success(game.copy(state = game.state.advanceRound()))
        override def undoStep(game: Game): Try[Game] =
          Success(game.copy(state = game.state.copy(round = game.state.round - 1)))
      }

      command.undoStep(game) shouldBe a[Success[?]]
      command.undoStep(game).get.state.round shouldBe 1
    }

    "support successful command execution" in {
      val game = TestGameFactory.game()
      val successCommand = new Command {
        override def doStep(game: Game): Try[Game] = 
          Success(game.copy(state = game.state.setPhase(Phase.ExecutingTurn)))
        override def undoStep(game: Game): Try[Game] = 
          Success(game.copy(state = game.state.setPhase(Phase.AwaitingInput)))
      }

      val result = successCommand.doStep(game)
      result shouldBe a[Success[?]]
      result.get.state.phase shouldBe Phase.ExecutingTurn
    }

    "support failed command execution" in {
      val game = TestGameFactory.game()
      val failCommand = new Command {
        override def doStep(game: Game): Try[Game] = 
          Failure(new Exception("Command failed"))
        override def undoStep(game: Game): Try[Game] = 
          Success(game)
      }

      val result = failCommand.doStep(game)
      result shouldBe a[Failure[?]]
    }

    "support successful undo execution" in {
      val game = TestGameFactory.game().copy(state = GameState().advanceRound())
      val command = new Command {
        override def doStep(game: Game): Try[Game] =
          Success(game.copy(state = game.state.advanceRound()))
        override def undoStep(game: Game): Try[Game] =
          Success(game.copy(state = game.state.copy(round = 1)))
      }

      val result = command.undoStep(game)
      result shouldBe a[Success[?]]
      result.get.state.round shouldBe 1
    }

    "support failed undo execution" in {
      val game = TestGameFactory.game()
      val failUndoCommand = new Command {
        override def doStep(game: Game): Try[Game] = 
          Success(game)
        override def undoStep(game: Game): Try[Game] = 
          Failure(new Exception("Undo failed"))
      }

      val result = failUndoCommand.undoStep(game)
      result shouldBe a[Failure[?]]
    }

    "work with DummyCommand implementation" in {
      val game = TestGameFactory.game()
      val dummy = new DummyCommand()

      val doResult = dummy.doStep(game)
      doResult shouldBe a[Success[?]]
      doResult.get.state.round shouldBe 2

      val undoResult = dummy.undoStep(doResult.get)
      undoResult shouldBe a[Success[?]]
      undoResult.get.state.round shouldBe 1
    }

    "allow chaining multiple commands" in {
      val game = TestGameFactory.game()
      val cmd1 = new DummyCommand()
      val cmd2 = new DummyCommand()

      val result = for {
        game1 <- cmd1.doStep(game)
        game2 <- cmd2.doStep(game1)
      } yield game2

      result shouldBe a[Success[?]]
      result.get.state.round shouldBe 3
    }

    "handle command with state transformation" in {
      val game = TestGameFactory.game()
      val statusCommand = new Command {
        override def doStep(game: Game): Try[Game] = 
          Success(game.copy(state = game.state.setStatus(GameStatus.Paused)))
        override def undoStep(game: Game): Try[Game] = 
          Success(game.copy(state = game.state.setStatus(GameStatus.Running)))
      }

      val result = statusCommand.doStep(game)
      result.get.state.status shouldBe GameStatus.Paused
      
      val undoResult = statusCommand.undoStep(result.get)
      undoResult.get.state.status shouldBe GameStatus.Running
    }

    "preserve game model during state-only transformations" in {
      val game = TestGameFactory.game()
      val command = new DummyCommand()

      val result = command.doStep(game)
      result.get.model shouldBe game.model
      result.get.model.players shouldBe game.model.players
      result.get.model.servers shouldBe game.model.servers
    }

    "support command composition pattern" in {
      val game = TestGameFactory.game()
      
      val compositeCommand = new Command {
        private val cmd1 = new DummyCommand()
        
        override def doStep(game: Game): Try[Game] = 
          cmd1.doStep(game).flatMap(g => 
            Success(g.copy(state = g.state.setPhase(Phase.ExecutingTurn)))
          )
        
        override def undoStep(game: Game): Try[Game] = 
          Success(game.copy(state = game.state.setPhase(Phase.AwaitingInput))).flatMap(g =>
            cmd1.undoStep(g)
          )
      }

      val result = compositeCommand.doStep(game)
      result shouldBe a[Success[?]]
      result.get.state.round shouldBe 2
      result.get.state.phase shouldBe Phase.ExecutingTurn
    }
  }
