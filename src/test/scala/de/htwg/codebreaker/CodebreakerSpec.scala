// src/test/scala/de/htwg/codebreaker/CodebreakerSpec.scala
package de.htwg.codebreaker

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CodebreakerSpec extends AnyWordSpec with Matchers:

  "The Codebreaker object" should {
    "exist and be accessible" in {
      // Simply verify the object can be referenced
      Codebreaker should not be null
    }

    "have a main method defined" in {
      // Verify the main method exists via reflection
      val mainMethod = Codebreaker.getClass.getMethods.find(_.getName == "main")
      mainMethod should not be empty
    }
  }
