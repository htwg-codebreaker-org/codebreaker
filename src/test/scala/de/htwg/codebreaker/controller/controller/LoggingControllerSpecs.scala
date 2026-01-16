package de.htwg.codebreaker.controller.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.codebreaker.controller.{Command, TestGameFactory, FakeFileIO, DummyCommand}
import de.htwg.codebreaker.model.game._
import de.htwg.codebreaker.util.Observer

class LoggingControllerSpec extends AnyWordSpec with Matchers {

  "LoggingController" should {

    "delegate game query" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      logging.game shouldBe inner.game
    }

    "delegate getPlayers query" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      logging.getPlayers shouldBe inner.getPlayers
    }

    "delegate getServers query" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      logging.getServers shouldBe inner.getServers
    }

    "delegate getMapData query" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      logging.getMapData() shouldBe inner.getMapData()
    }

    "delegate getState query" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      logging.getState shouldBe inner.getState
    }

    "delegate canUndo query" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      logging.canUndo shouldBe inner.canUndo
    }

    "delegate canRedo query" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      logging.canRedo shouldBe inner.canRedo
    }

    "delegate doAndRemember command" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      val cmd = new DummyCommand
      
      logging.doAndRemember(cmd)
      
      inner.canUndo shouldBe true
      inner.getState.round shouldBe 1
    }

    "delegate undo command" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      inner.doAndRemember(new DummyCommand)
      logging.undo()
      
      inner.getState.round shouldBe 0
    }

    "delegate redo command" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      inner.doAndRemember(new DummyCommand)
      inner.undo()
      logging.redo()
      
      inner.getState.round shouldBe 1
    }

    "delegate advanceRound" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      logging.advanceRound()
      
      inner.getState.round shouldBe 1
    }

    "delegate setPhase" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      logging.setPhase(Phase.ExecutingTurn)
      
      inner.getState.phase shouldBe Phase.ExecutingTurn
    }

    "delegate setStatus" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      logging.setStatus(GameStatus.Paused)
      
      inner.getState.status shouldBe GameStatus.Paused
    }

    "delegate setGame" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      val newGame = TestGameFactory.game()
      
      logging.setGame(newGame)
      
      inner.game shouldBe newGame
    }

    "delegate save" in {
      val fileIO = new FakeFileIO()
      val inner = Controller(TestGameFactory.game(), fileIO)
      val logging = new LoggingController(inner)
      
      inner.advanceRound()
      logging.save()
      
      // Verify save worked by loading in a new controller
      val inner2 = Controller(TestGameFactory.game(), fileIO)
      inner2.load()
      inner2.getState.round shouldBe 1
    }

    "delegate load" in {
      val fileIO = new FakeFileIO()
      val inner = Controller(TestGameFactory.game(), fileIO)
      val logging = new LoggingController(inner)
      
      inner.advanceRound()
      inner.save()
      
      val inner2 = Controller(TestGameFactory.game(), fileIO)
      val logging2 = new LoggingController(inner2)
      logging2.load()
      
      inner2.getState.round shouldBe 1
    }

    "delegate add observer" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      var notified = false
      
      val observer = new Observer {
        override def update(): Unit = notified = true
      }
      
      logging.add(observer)
      inner.advanceRound()
      
      notified shouldBe true
    }

    "delegate remove observer" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      var notified = false
      
      val observer = new Observer {
        override def update(): Unit = notified = true
      }
      
      logging.add(observer)
      logging.remove(observer)
      inner.advanceRound()
      
      notified shouldBe false
    }

    "work as transparent decorator" in {
      val inner = Controller(TestGameFactory.game(), new FakeFileIO())
      val logging = new LoggingController(inner)
      
      // All operations should work through the decorator
      logging.doAndRemember(new DummyCommand)
      logging.getState.round shouldBe 1
      
      logging.undo()
      logging.getState.round shouldBe 0
      
      logging.redo()
      logging.getState.round shouldBe 1
    }
  }
}