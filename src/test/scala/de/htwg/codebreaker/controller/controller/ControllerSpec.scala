package de.htwg.codebreaker.controller.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.util.{Success, Failure}

import de.htwg.codebreaker.model.game.{Game, GameState, Phase, GameStatus}
import de.htwg.codebreaker.model.builder.GameFactory
import de.htwg.codebreaker.controller.Command
import de.htwg.codebreaker.persistence.FileIOInterface
import de.htwg.codebreaker.util.Observer

class ControllerSpec extends AnyWordSpec with Matchers:

  // Mock FileIO for testing
  class MockFileIO extends FileIOInterface:
    var savedGame: Option[Game] = None
    var loadedGame: Option[Game] = None
    var shouldFailSave: Boolean = false
    var shouldFailLoad: Boolean = false

    def save(game: Game): scala.util.Try[Unit] =
      if shouldFailSave then
        Failure(new Exception("Save failed"))
      else
        savedGame = Some(game)
        Success(())

    def load(): scala.util.Try[Game] =
      if shouldFailLoad then
        Failure(new Exception("Load failed"))
      else
        loadedGame match
          case Some(game) => Success(game)
          case None => Failure(new Exception("No game to load"))

  // Simple test command that increments round number
  case class TestCommand() extends Command:
    def doStep(game: Game): scala.util.Try[Game] =
      Success(game.copy(state = game.state.copy(round = game.state.round + 1)))

    def undoStep(game: Game): scala.util.Try[Game] =
      Success(game.copy(state = game.state.copy(round = game.state.round - 1)))

  // Failing command for error testing
  case class FailingCommand() extends Command:
    def doStep(game: Game): scala.util.Try[Game] =
      Failure(new Exception("Command failed"))

    def undoStep(game: Game): scala.util.Try[Game] =
      Failure(new Exception("Undo failed"))

  // Test observer
  class TestObserver extends Observer:
    var updateCount: Int = 0
    def update(): Unit = updateCount += 1

  def createController(): (Controller, MockFileIO) =
    val fileIO = new MockFileIO()
    val game = GameFactory.default()
    val controller = new Controller(game, fileIO)
    (controller, fileIO)

  "Controller" should {

    "be created with initial game state" in {
      val (controller, _) = createController()

      controller.game should not be null
      controller.getPlayers should not be empty
      controller.getServers should not be empty
      controller.getState should not be null
    }

    "provide access to players" in {
      val (controller, _) = createController()

      val players = controller.getPlayers
      players should not be empty
      players.length should be >= 1
    }

    "provide access to servers" in {
      val (controller, _) = createController()

      val servers = controller.getServers
      servers should not be empty
    }

    "provide access to map data" in {
      val (controller, _) = createController()

      val mapData = controller.getMapData()
      mapData should not be empty
      mapData.head should not be empty
    }

    "provide access to game state" in {
      val (controller, _) = createController()

      val state = controller.getState
      state.round should be >= 0
    }

    "have no undo/redo available initially" in {
      val (controller, _) = createController()

      controller.canUndo shouldBe false
      controller.canRedo shouldBe false
    }

    "execute command with doAndRemember" in {
      val (controller, _) = createController()
      val initialRound = controller.game.state.round

      controller.doAndRemember(TestCommand())

      controller.game.state.round shouldBe initialRound + 1
      controller.canUndo shouldBe true
      controller.canRedo shouldBe false
    }

    "save game after doAndRemember" in {
      val (controller, fileIO) = createController()

      controller.doAndRemember(TestCommand())

      fileIO.savedGame shouldBe defined
      fileIO.savedGame.get.state.round shouldBe controller.game.state.round
    }

    "execute command with doAndForget" in {
      val (controller, _) = createController()
      val initialRound = controller.game.state.round

      controller.doAndForget(TestCommand())

      controller.game.state.round shouldBe initialRound + 1
      controller.canUndo shouldBe false
      controller.canRedo shouldBe false
    }

    "clear undo/redo stacks with doAndForget" in {
      val (controller, _) = createController()

      controller.doAndRemember(TestCommand())
      controller.canUndo shouldBe true

      controller.doAndForget(TestCommand())
      controller.canUndo shouldBe false
      controller.canRedo shouldBe false
    }

    "undo a command" in {
      val (controller, _) = createController()
      val initialRound = controller.game.state.round

      controller.doAndRemember(TestCommand())
      controller.game.state.round shouldBe initialRound + 1

      controller.undo()
      controller.game.state.round shouldBe initialRound
      controller.canUndo shouldBe false
      controller.canRedo shouldBe true
    }

    "redo a command" in {
      val (controller, _) = createController()
      val initialRound = controller.game.state.round

      controller.doAndRemember(TestCommand())
      controller.undo()
      controller.game.state.round shouldBe initialRound

      controller.redo()
      controller.game.state.round shouldBe initialRound + 1
      controller.canUndo shouldBe true
      controller.canRedo shouldBe false
    }

    "handle multiple undo/redo operations" in {
      val (controller, _) = createController()
      val initialRound = controller.game.state.round

      controller.doAndRemember(TestCommand())
      controller.doAndRemember(TestCommand())
      controller.doAndRemember(TestCommand())

      controller.game.state.round shouldBe initialRound + 3

      controller.undo()
      controller.game.state.round shouldBe initialRound + 2

      controller.undo()
      controller.game.state.round shouldBe initialRound + 1

      controller.redo()
      controller.game.state.round shouldBe initialRound + 2
    }

    "clear redo stack when new command is executed after undo" in {
      val (controller, _) = createController()

      controller.doAndRemember(TestCommand())
      controller.doAndRemember(TestCommand())
      controller.undo()

      controller.canRedo shouldBe true
      controller.doAndRemember(TestCommand())
      controller.canRedo shouldBe false
    }

    "do nothing when undo is called with empty stack" in {
      val (controller, _) = createController()
      val initialRound = controller.game.state.round

      controller.undo()

      controller.game.state.round shouldBe initialRound
      controller.canUndo shouldBe false
    }

    "do nothing when redo is called with empty stack" in {
      val (controller, _) = createController()
      val initialRound = controller.game.state.round

      controller.redo()

      controller.game.state.round shouldBe initialRound
      controller.canRedo shouldBe false
    }

    "handle failing command gracefully in doAndRemember" in {
      val (controller, _) = createController()
      val initialRound = controller.game.state.round

      controller.doAndRemember(FailingCommand())

      controller.game.state.round shouldBe initialRound
      controller.canUndo shouldBe false
    }

    "handle failing command gracefully in doAndForget" in {
      val (controller, _) = createController()
      val initialRound = controller.game.state.round

      controller.doAndForget(FailingCommand())

      controller.game.state.round shouldBe initialRound
      controller.canUndo shouldBe false
    }

    "set new phase" in {
      val (controller, _) = createController()

      controller.setPhase(Phase.AwaitingInput)
      controller.game.state.phase shouldBe Phase.AwaitingInput

      controller.setPhase(Phase.ExecutingTurn)
      controller.game.state.phase shouldBe Phase.ExecutingTurn
    }

    "set new status" in {
      val (controller, _) = createController()

      controller.setStatus(GameStatus.Running)
      controller.game.state.status shouldBe GameStatus.Running

      controller.setStatus(GameStatus.GameOver)
      controller.game.state.status shouldBe GameStatus.GameOver
    }

    "set new game" in {
      val (controller, _) = createController()
      val newGame = GameFactory.default()

      controller.setGame(newGame)

      controller.game shouldBe newGame
      controller.canUndo shouldBe false
      controller.canRedo shouldBe false
    }

    "save game successfully" in {
      val (controller, fileIO) = createController()

      controller.save()

      fileIO.savedGame shouldBe defined
      fileIO.savedGame.get shouldBe controller.game
    }

    "handle save failure gracefully" in {
      val (controller, fileIO) = createController()
      fileIO.shouldFailSave = true

      noException should be thrownBy controller.save()
    }

    "load game successfully" in {
      val (controller, fileIO) = createController()
      val gameToLoad = GameFactory.default()
      fileIO.loadedGame = Some(gameToLoad)

      controller.load()

      controller.game shouldBe gameToLoad
    }

    "handle load failure gracefully" in {
      val (controller, fileIO) = createController()
      fileIO.shouldFailLoad = true

      noException should be thrownBy controller.load()
    }

    "notify observers on doAndRemember" in {
      val (controller, _) = createController()
      val observer = new TestObserver()
      controller.add(observer)

      observer.updateCount shouldBe 0
      controller.doAndRemember(TestCommand())
      observer.updateCount shouldBe 1
    }

    "notify observers on doAndForget" in {
      val (controller, _) = createController()
      val observer = new TestObserver()
      controller.add(observer)

      observer.updateCount shouldBe 0
      controller.doAndForget(TestCommand())
      observer.updateCount shouldBe 1
    }

    "notify observers on undo" in {
      val (controller, _) = createController()
      val observer = new TestObserver()
      controller.add(observer)

      controller.doAndRemember(TestCommand())
      observer.updateCount shouldBe 1

      controller.undo()
      observer.updateCount shouldBe 2
    }

    "notify observers on redo" in {
      val (controller, _) = createController()
      val observer = new TestObserver()
      controller.add(observer)

      controller.doAndRemember(TestCommand())
      controller.undo()
      observer.updateCount shouldBe 2

      controller.redo()
      observer.updateCount shouldBe 3
    }

    "notify observers on setPhase" in {
      val (controller, _) = createController()
      val observer = new TestObserver()
      controller.add(observer)

      observer.updateCount shouldBe 0
      controller.setPhase(Phase.AwaitingInput)
      observer.updateCount shouldBe 1
    }

    "notify observers on setStatus" in {
      val (controller, _) = createController()
      val observer = new TestObserver()
      controller.add(observer)

      observer.updateCount shouldBe 0
      controller.setStatus(GameStatus.Running)
      observer.updateCount shouldBe 1
    }

    "notify observers on setGame" in {
      val (controller, _) = createController()
      val observer = new TestObserver()
      controller.add(observer)

      observer.updateCount shouldBe 0
      controller.setGame(GameFactory.default())
      observer.updateCount shouldBe 1
    }

    "notify observers on load" in {
      val (controller, fileIO) = createController()
      val observer = new TestObserver()
      controller.add(observer)
      fileIO.loadedGame = Some(GameFactory.default())

      observer.updateCount shouldBe 0
      controller.load()
      observer.updateCount shouldBe 1
    }

    "support multiple observers" in {
      val (controller, _) = createController()
      val observer1 = new TestObserver()
      val observer2 = new TestObserver()
      val observer3 = new TestObserver()

      controller.add(observer1)
      controller.add(observer2)
      controller.add(observer3)

      controller.doAndRemember(TestCommand())

      observer1.updateCount shouldBe 1
      observer2.updateCount shouldBe 1
      observer3.updateCount shouldBe 1
    }

    "remove observers" in {
      val (controller, _) = createController()
      val observer = new TestObserver()

      controller.add(observer)
      controller.doAndRemember(TestCommand())
      observer.updateCount shouldBe 1

      controller.remove(observer)
      controller.doAndRemember(TestCommand())
      observer.updateCount shouldBe 1
    }

    "get completed actions for current player" in {
      val (controller, _) = createController()

      val actions = controller.getCompletedActionsForCurrentPlayer()
      actions shouldBe a[List[?]]
    }

    "return empty list for completed actions when no current player" in {
      val (controller, _) = createController()
      val gameWithNoCurrentPlayer = controller.game.copy(
        state = controller.game.state.copy(currentPlayerIndex = None)
      )
      controller.setGame(gameWithNoCurrentPlayer)

      val actions = controller.getCompletedActionsForCurrentPlayer()
      actions shouldBe empty
    }

    "return empty list for completed actions when player index is invalid" in {
      val (controller, _) = createController()
      val gameWithInvalidIndex = controller.game.copy(
        state = controller.game.state.copy(currentPlayerIndex = Some(999))
      )
      controller.setGame(gameWithInvalidIndex)

      val actions = controller.getCompletedActionsForCurrentPlayer()
      actions shouldBe empty
    }

    "maintain game state integrity after multiple operations" in {
      val (controller, _) = createController()
      val initialPlayers = controller.getPlayers.length
      val initialServers = controller.getServers.length

      controller.doAndRemember(TestCommand())
      controller.doAndRemember(TestCommand())
      controller.undo()
      controller.redo()
      controller.setPhase(Phase.ExecutingTurn)
      controller.setStatus(GameStatus.Running)

      controller.getPlayers.length shouldBe initialPlayers
      controller.getServers.length shouldBe initialServers
    }
  }
