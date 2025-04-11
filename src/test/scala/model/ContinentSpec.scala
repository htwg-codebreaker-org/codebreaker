package model
import model.Continent
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ContinentSpec extends AnyWordSpec with Matchers {

  "Continent.short" should {
    "return correct short codes for all continents" in {
      Continent.NorthAmerica.short shouldBe "NA"
      Continent.SouthAmerica.short shouldBe "SA"
      Continent.Europe.short shouldBe "EU"
      Continent.Africa.short shouldBe "AF"
      Continent.Asia.short shouldBe "AS"
      Continent.Oceania.short shouldBe "OC"
      Continent.Antarctica.short shouldBe "AN"
      Continent.Ocean.short shouldBe "XX"
    }
  }

  "Continent.isLand" should {
    "return false for Ocean" in {
      Continent.Ocean.isLand shouldBe false
    }

    "return true for all other continents" in {
      Continent.values.filterNot(_ == Continent.Ocean).foreach { c =>
        c.isLand shouldBe true
      }
    }
  }
}
