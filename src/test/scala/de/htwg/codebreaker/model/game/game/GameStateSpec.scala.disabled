package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class GameStateSpec extends AnyWordSpec with Matchers:

  "GameStatus enum" should {

    "have all statuses defined" in {
      val statuses = GameStatus.values
      statuses should contain allOf (
        GameStatus.Running,
        GameStatus.Paused,
        GameStatus.GameOver
      )
    }
  }

  "Phase enum" should {

    "have all phases defined" in {
      val phases = Phase.values
      phases should contain allOf (
        Phase.AwaitingInput,
        Phase.ExecutingTurn,
        Phase.FinishedTurn
      )
    }
  }

  "GameState" should {

    "be created with default values" in {
      val state = GameState()

      state.currentPlayerIndex shouldBe None
      state.status shouldBe GameStatus.Running
      state.phase shouldBe Phase.AwaitingInput
      state.round shouldBe 0
    }

    "be created with custom values" in {
      val state = GameState(
        currentPlayerIndex = Some(2),
        status = GameStatus.Paused,
        phase = Phase.ExecutingTurn,
        round = 5
      )

      state.currentPlayerIndex shouldBe Some(2)
      state.status shouldBe GameStatus.Paused
      state.phase shouldBe Phase.ExecutingTurn
      state.round shouldBe 5
    }

    "advance to next player" in {
      val state = GameState(currentPlayerIndex = Some(0))
      
      val next = state.nextPlayer(3)
      next.currentPlayerIndex shouldBe Some(1)
    }

    "wrap around to first player" in {
      val state = GameState(currentPlayerIndex = Some(2))
      
      val next = state.nextPlayer(3)
      next.currentPlayerIndex shouldBe Some(0)
    }

    "set initial player when None" in {
      val state = GameState(currentPlayerIndex = None)
      
      val next = state.nextPlayer(4)
      next.currentPlayerIndex shouldBe Some(0)
    }

    "advance round" in {
      val state = GameState(round = 1)
      
      val next = state.advanceRound()
      next.round shouldBe 2
    }

    "advance round multiple times" in {
      val state = GameState(round = 1)
      
      val state2 = state.advanceRound()
      val state3 = state2.advanceRound()
      val state4 = state3.advanceRound()
      
      state4.round shouldBe 4
    }

    "set status" in {
      val state = GameState(status = GameStatus.Running)
      
      val paused = state.setStatus(GameStatus.Paused)
      paused.status shouldBe GameStatus.Paused
      
      val gameOver = paused.setStatus(GameStatus.GameOver)
      gameOver.status shouldBe GameStatus.GameOver
    }

    "set phase" in {
      val state = GameState(phase = Phase.AwaitingInput)
      
      val executing = state.setPhase(Phase.ExecutingTurn)
      executing.phase shouldBe Phase.ExecutingTurn
      
      val finished = executing.setPhase(Phase.FinishedTurn)
      finished.phase shouldBe Phase.FinishedTurn
    }

    "not modify original when advancing round" in {
      val original = GameState(round = 1)
      val next = original.advanceRound()
      
      original.round shouldBe 1
      next.round shouldBe 2
    }

    "not modify original when setting status" in {
      val original = GameState(status = GameStatus.Running)
      val modified = original.setStatus(GameStatus.Paused)
      
      original.status shouldBe GameStatus.Running
      modified.status shouldBe GameStatus.Paused
    }

    "not modify original when setting phase" in {
      val original = GameState(phase = Phase.AwaitingInput)
      val modified = original.setPhase(Phase.ExecutingTurn)
      
      original.phase shouldBe Phase.AwaitingInput
      modified.phase shouldBe Phase.ExecutingTurn
    }

    "not modify original when advancing player" in {
      val original = GameState(currentPlayerIndex = Some(0))
      val modified = original.nextPlayer(3)
      
      original.currentPlayerIndex shouldBe Some(0)
      modified.currentPlayerIndex shouldBe Some(1)
    }

    "handle single player game" in {
      val state = GameState(currentPlayerIndex = Some(0))
      
      val next = state.nextPlayer(1)
      next.currentPlayerIndex shouldBe Some(0)
    }

    "cycle through all players" in {
      val state = GameState(currentPlayerIndex = Some(0))
      val totalPlayers = 4
      
      val p1 = state.nextPlayer(totalPlayers)
      p1.currentPlayerIndex shouldBe Some(1)
      
      val p2 = p1.nextPlayer(totalPlayers)
      p2.currentPlayerIndex shouldBe Some(2)
      
      val p3 = p2.nextPlayer(totalPlayers)
      p3.currentPlayerIndex shouldBe Some(3)
      
      val p0 = p3.nextPlayer(totalPlayers)
      p0.currentPlayerIndex shouldBe Some(0)
    }

    "support combined state changes" in {
      val state = GameState()
      
      val updated = state
        .advanceRound()
        .setPhase(Phase.ExecutingTurn)
        .setStatus(GameStatus.Paused)
        .nextPlayer(2)
      
      updated.round shouldBe 1
      updated.phase shouldBe Phase.ExecutingTurn
      updated.status shouldBe GameStatus.Paused
      updated.currentPlayerIndex shouldBe Some(0)
    }
  }
