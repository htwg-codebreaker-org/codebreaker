package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameStateSpec extends AnyWordSpec with Matchers {

  "GameState" should {

    "initialize with default values" in {
      val state = GameState()

      state.currentPlayerIndex shouldBe None
      state.status shouldBe GameStatus.Running
      state.phase shouldBe Phase.AwaitingInput
      state.round shouldBe 1
    }

    "advance to next player correctly" in {
      val stateWithNone = GameState(None)
      val updated1 = stateWithNone.nextPlayer(3)
      updated1.currentPlayerIndex shouldBe Some(0)

      val stateWithSome = GameState(Some(1))
      val updated2 = stateWithSome.nextPlayer(3)
      updated2.currentPlayerIndex shouldBe Some(2)
    }

    "advance round correctly" in {
      val state = GameState(round = 2)
      val updated = state.advanceRound()
      updated.round shouldBe 3
    }

    "set status and phase" in {
      val state = GameState()
      val withStatus = state.setStatus(GameStatus.Paused)
      val withPhase = state.setPhase(Phase.ExecutingTurn)

      withStatus.status shouldBe GameStatus.Paused
      withPhase.phase shouldBe Phase.ExecutingTurn
    }

    "support equality and toString" in {
      val s1 = GameState(Some(1), GameStatus.Running, Phase.AwaitingInput, 1)
      val s2 = GameState(Some(1), GameStatus.Running, Phase.AwaitingInput, 1)

      s1 shouldBe s2
      s1.toString should include ("Running")
    }
  }
}
