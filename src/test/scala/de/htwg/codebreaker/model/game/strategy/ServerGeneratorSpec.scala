package de.htwg.codebreaker.model.game.strategy

import de.htwg.codebreaker.model._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ServerGeneratorSpec extends AnyWordSpec with Matchers {

  "ServerGenerator" should {

    "generate fixed servers from blueprints with valid properties" in {
      val map     = WorldMap.defaultMap
      val servers = ServerGenerator.generateFixedServers(map)

      servers should not be empty
      all(servers.map(_.tile)) should not be null
      all(servers.map(_.serverType)) should not be null
    }

    "generate side servers for a given continent with correct types" in {
      val map   = WorldMap.defaultMap
      val fixed = ServerGenerator.generateFixedServers(map)
      val side  = ServerGenerator.generateSideServersFor(Continent.Europe, map, fixed, 2)

      side should not be empty
      all(side.map(_.serverType)) shouldBe ServerType.Side
      all(side.map(_.tile.continent)) shouldBe Continent.Europe
    }

    "select tiles that meet minimum distance requirements" in {
      val map         = WorldMap.defaultMap
      val europeTiles = map.tilesOf(Continent.Europe)
      val selected    = ServerGenerator.pickNonCloseTiles(europeTiles, 3, 2, Nil)

      selected.size should be <= 3
      selected.combinations(2).foreach {
        case Seq(a, b) =>
          val dist = ServerGenerator.distance(a, b)
          dist should be >= 2
        case _         => // do nothing
      }
    }

    "generate number within range using rngIn" in {
      val values = (1 to 100).map(_ => ServerGenerator.rngIn(5 -> 10)).toSet
      values.min should be >= 5
      values.max should be <= 10
    }
  }
}
