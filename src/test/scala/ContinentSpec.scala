package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ContinentSpec extends AnyWordSpec with Matchers {

  "A Continent" should {

    "correctly report if it is land or not" in {
      val landContinents = List(
        Continent.NorthAmerica,
        Continent.SouthAmerica,
        Continent.Europe,
        Continent.Africa,
        Continent.Asia,
        Continent.Oceania,
        Continent.Antarctica
      )
      val nonLandContinents = List(Continent.Ocean)

      landContinents.foreach(_.isLand shouldBe true)
      nonLandContinents.foreach(_.isLand shouldBe false)
    }

    "return the correct short codes" in {
      Continent.NorthAmerica.short shouldBe "NA"
      Continent.SouthAmerica.short shouldBe "SA"
      Continent.Europe.short        shouldBe "EU"
      Continent.Africa.short        shouldBe "AF"
      Continent.Asia.short          shouldBe "AS"
      Continent.Oceania.short       shouldBe "OC"
      Continent.Antarctica.short    shouldBe "AN"
      Continent.Ocean.short         shouldBe "XX"
    }
  }
}
