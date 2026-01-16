package de.htwg.codebreaker.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ContinentSpec extends AnyWordSpec with Matchers:

  "Continent enum" should {

    "have all continents defined" in {
      val continents = Continent.values
      continents should contain allOf (
        Continent.NorthAmerica,
        Continent.SouthAmerica,
        Continent.Europe,
        Continent.Africa,
        Continent.Asia,
        Continent.Oceania,
        Continent.Antarctica,
        Continent.Ocean
      )
    }

    "identify land continents correctly" in {
      Continent.NorthAmerica.isLand shouldBe true
      Continent.SouthAmerica.isLand shouldBe true
      Continent.Europe.isLand shouldBe true
      Continent.Africa.isLand shouldBe true
      Continent.Asia.isLand shouldBe true
      Continent.Oceania.isLand shouldBe true
      Continent.Antarctica.isLand shouldBe true
    }

    "identify ocean as not land" in {
      Continent.Ocean.isLand shouldBe false
    }

    "return correct short codes for NorthAmerica" in {
      Continent.NorthAmerica.short shouldBe "NA"
    }

    "return correct short codes for SouthAmerica" in {
      Continent.SouthAmerica.short shouldBe "SA"
    }

    "return correct short codes for Europe" in {
      Continent.Europe.short shouldBe "EU"
    }

    "return correct short codes for Africa" in {
      Continent.Africa.short shouldBe "AF"
    }

    "return correct short codes for Asia" in {
      Continent.Asia.short shouldBe "AS"
    }

    "return correct short codes for Oceania" in {
      Continent.Oceania.short shouldBe "OC"
    }

    "return correct short codes for Antarctica" in {
      Continent.Antarctica.short shouldBe "AN"
    }

    "return correct short codes for Ocean" in {
      Continent.Ocean.short shouldBe "~~"
    }
  }
