package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameStateSpec extends AnyWordSpec with Matchers {

  "GameState" should {

    "initialize with fallback enum access" in {
      val status = GameStatus.valueOf("Running")
      val phase = Phase.valueOf("AwaitingInput")

      val state = GameState(
        None,
        status,
        phase,
        1
      )

      state.currentPlayerIndex shouldBe None
      state.status shouldBe status
      state.phase shouldBe phase
      state.round shouldBe 1
    }

    "copy state and change values" in {
      val original = GameState(
        None,
        GameStatus.valueOf("Running"),
        Phase.valueOf("AwaitingInput"),
        1
      )

      val updated = original.copy(
        currentPlayerIndex = Some(1),
        status = GameStatus.valueOf("Paused"),
        phase = Phase.valueOf("ExecutingTurn"),
        round = 3
      )

      updated.currentPlayerIndex shouldBe Some(1)
      updated.status.toString shouldBe "Paused"
      updated.phase.toString shouldBe "ExecutingTurn"
      updated.round shouldBe 3
    }
  }
}
