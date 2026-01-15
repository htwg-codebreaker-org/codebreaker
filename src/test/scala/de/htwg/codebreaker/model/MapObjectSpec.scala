package de.htwg.codebreaker.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import MapObject._

class MapObjectSpec extends AnyWordSpec with Matchers {

  "MapObject types" should {
    "create PlayerOnTile correctly" in {
      val obj = PlayerOnTile(1)
      obj.index shouldBe 1
    }

    "create ServerOnTile with continent" in {
      val obj = ServerOnTile(0, ServerType.Side, Continent.Africa)
      obj.continent shouldBe Continent.Africa
    }

    "create PlayerAndServerTile correctly" in {
      val obj = PlayerAndServerTile(0, 1, ServerType.Firm, Continent.Europe)
      obj.serverType shouldBe ServerType.Firm
    }

    "create EmptyTile with continent" in {
      val empty = EmptyTile(Continent.Oceania)
      empty.continent shouldBe Continent.Oceania
    }
  }
}
