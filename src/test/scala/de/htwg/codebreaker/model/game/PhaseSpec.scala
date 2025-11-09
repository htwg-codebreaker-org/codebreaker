package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PhaseSpec extends AnyWordSpec with Matchers {

  "Phase enum" should {
    "have AwaitingInput value" in {
      Phase.AwaitingInput should not be null
    }

    "have ExecutingTurn value" in {
      Phase.ExecutingTurn should not be null
    }

    "support equality" in {
      Phase.AwaitingInput shouldBe Phase.AwaitingInput
      Phase.ExecutingTurn shouldBe Phase.ExecutingTurn
      Phase.AwaitingInput should not be Phase.ExecutingTurn
    }
  }
}
