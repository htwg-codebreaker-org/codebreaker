package de.htwg.codebreaker.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.codebreaker.controller.controller.Controller
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.util.Observer
import scala.util.{Failure, Success}

class ControllerSpec extends AnyWordSpec with Matchers:

  "Controller" should {

    "return initial game data" in {
      val fileIO = new FakeFileIO()
      val controller = Controller(TestGameFactory.game(), fileIO)

      controller.getPlayers should have size 1
      controller.getServers should have size 1
      controller.getState.round shouldBe 0
    }

    "return the current game" in {
      val fileIO = new FakeFileIO()
      val controller = Controller(TestGameFactory.game(), fileIO)
      
      controller.game should not be null
      controller.game.model.players should have size 1
    }

    "return map data" in {
      val fileIO = new FakeFileIO()
      val controller = Controller(TestGameFactory.game(), fileIO)
      
      val mapData = controller.getMapData()
      mapData should not be null
      mapData.size should be > 0
    }

    "execute command and remember for undo" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val cmd = new DummyCommand

      controller.canUndo shouldBe false
      controller.doAndRemember(cmd)

      controller.canUndo shouldBe true
      controller.getState.round shouldBe 1
    }

    "handle failed command execution" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val failingCmd = new Command {
        override def doStep(game: Game): scala.util.Try[Game] = Failure(new Exception("Test failure"))
        override def undoStep(game: Game): scala.util.Try[Game] = Success(game)
      }

      val roundBefore = controller.getState.round
      controller.doAndRemember(failingCmd)
      
      controller.getState.round shouldBe roundBefore
      controller.canUndo shouldBe false
    }

    "undo last command" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val cmd = new DummyCommand

      controller.doAndRemember(cmd)
      controller.undo()

      controller.getState.round shouldBe 0
      controller.canRedo shouldBe true
    }

    "handle undo when stack is empty" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      
      controller.canUndo shouldBe false
      controller.undo() // Should not crash
      controller.getState.round shouldBe 0
    }

    "handle failed undo operation" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val failingUndoCmd = new Command {
        override def doStep(game: Game): scala.util.Try[Game] = 
          Success(game.copy(state = game.state.advanceRound()))
        override def undoStep(game: Game): scala.util.Try[Game] = 
          Failure(new Exception("Undo failed"))
      }

      controller.doAndRemember(failingUndoCmd)
      val roundAfterDo = controller.getState.round
      
      controller.undo()
      controller.getState.round shouldBe roundAfterDo // Should not change if undo failed
    }

    "redo last undone command" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val cmd = new DummyCommand

      controller.doAndRemember(cmd)
      controller.undo()
      controller.redo()

      controller.getState.round shouldBe 1
    }

    "handle redo when stack is empty" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      
      controller.canRedo shouldBe false
      controller.redo() // Should not crash
    }

    "handle failed redo operation" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val failingRedoCmd = new Command {
        override def doStep(game: Game): scala.util.Try[Game] = 
          Success(game.copy(state = game.state.advanceRound()))
        override def undoStep(game: Game): scala.util.Try[Game] = 
          Success(game.copy(state = game.state.copy(round = game.state.round - 1)))
      }

      controller.doAndRemember(failingRedoCmd)
      controller.undo()
      
      // Now make doStep fail for redo
      val controller2 = Controller(TestGameFactory.game(), new FakeFileIO())
      val failOnRedoCmd = new Command {
        var firstTime = true
        override def doStep(game: Game): scala.util.Try[Game] = 
          if (firstTime) {
            firstTime = false
            Success(game.copy(state = game.state.advanceRound()))
          } else {
            Failure(new Exception("Redo failed"))
          }
        override def undoStep(game: Game): scala.util.Try[Game] = 
          Success(game.copy(state = game.state.copy(round = game.state.round - 1)))
      }

      controller2.doAndRemember(failOnRedoCmd)
      controller2.undo()
      val roundAfterUndo = controller2.getState.round
      controller2.redo()
      controller2.getState.round shouldBe roundAfterUndo // Should not change if redo failed
    }

    "clear redo stack when new command is executed" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val cmd = new DummyCommand

      controller.doAndRemember(cmd)
      controller.undo()
      controller.canRedo shouldBe true

      controller.doAndRemember(cmd)
      controller.canRedo shouldBe false
    }

    "advance round directly" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())

      controller.advanceRound()
      controller.getState.round shouldBe 1
    }

    "set phase and status" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())

      controller.setPhase(Phase.ExecutingTurn)
      controller.setStatus(GameStatus.Paused)

      controller.getState.phase shouldBe Phase.ExecutingTurn
      controller.getState.status shouldBe GameStatus.Paused
    }

    "save and load game" in {
      val fileIO = new FakeFileIO()
      val controller = Controller(TestGameFactory.game(), fileIO)

      controller.advanceRound()
      controller.save()

      val controller2 = Controller(TestGameFactory.game(), fileIO)
      controller2.load()

      controller2.getState.round shouldBe 1
    }

    "handle save failure" in {
      val failingFileIO = new FakeFileIO() {
        override def save(game: Game): scala.util.Try[Unit] = 
          Failure(new Exception("Save failed"))
      }
      val controller = Controller(TestGameFactory.game(), failingFileIO)
      
      controller.save() // Should not crash
    }

    "handle load failure" in {
      val failingFileIO = new FakeFileIO() {
        override def load(): scala.util.Try[Game] = 
          Failure(new Exception("Load failed"))
      }
      val controller = Controller(TestGameFactory.game(), failingFileIO)
      
      controller.load() // Should not crash
    }

    "reset history when setting a new game" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      val cmd = new DummyCommand

      controller.doAndRemember(cmd)
      controller.canUndo shouldBe true

      controller.setGame(TestGameFactory.game())
      controller.canUndo shouldBe false
      controller.canRedo shouldBe false
    }

    "add and remove observers" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      var notified = false
      
      val observer = new Observer {
        override def update(): Unit = notified = true
      }

      controller.add(observer)
      controller.advanceRound()
      notified shouldBe true

      notified = false
      controller.remove(observer)
      controller.advanceRound()
      notified shouldBe false
    }

    "notify observers on setPhase" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      var notified = false
      
      val observer = new Observer {
        override def update(): Unit = notified = true
      }

      controller.add(observer)
      controller.setPhase(Phase.ExecutingTurn)
      notified shouldBe true
    }

    "notify observers on setStatus" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      var notified = false
      
      val observer = new Observer {
        override def update(): Unit = notified = true
      }

      controller.add(observer)
      controller.setStatus(GameStatus.Paused)
      notified shouldBe true
    }

    "notify observers on setGame" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      var notified = false
      
      val observer = new Observer {
        override def update(): Unit = notified = true
      }

      controller.add(observer)
      controller.setGame(TestGameFactory.game())
      notified shouldBe true
    }

    "notify observers on doAndRemember" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      var notified = false
      
      val observer = new Observer {
        override def update(): Unit = notified = true
      }

      controller.add(observer)
      controller.doAndRemember(new DummyCommand)
      notified shouldBe true
    }

    "notify observers on undo" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      var notifyCount = 0
      
      val observer = new Observer {
        override def update(): Unit = notifyCount += 1
      }

      controller.add(observer)
      controller.doAndRemember(new DummyCommand)
      notifyCount shouldBe 1
      
      controller.undo()
      notifyCount shouldBe 2
    }

    "notify observers on redo" in {
      val controller = Controller(TestGameFactory.game(), new FakeFileIO())
      var notifyCount = 0
      
      val observer = new Observer {
        override def update(): Unit = notifyCount += 1
      }

      controller.add(observer)
      controller.doAndRemember(new DummyCommand)
      controller.undo()
      notifyCount shouldBe 2
      
      controller.redo()
      notifyCount shouldBe 3
    }
  }
