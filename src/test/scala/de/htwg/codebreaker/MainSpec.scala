package de.htwg.codebreaker

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


import de.htwg.codebreaker.runGame

class MainSpec extends AnyWordSpec with Matchers {

  "runGame" should {
    "start and not throw any exception" in {
      noException should be thrownBy runGame()
    }
  }

}
