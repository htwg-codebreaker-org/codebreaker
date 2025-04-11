package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ServerGeneratorSpec extends AnyWordSpec with Matchers {

  val map = WorldMap.defaultMap
  val continent = Continent.Europe

  "ServerGenerator" should {

    "generate between 3 and 6 side servers" in {
      val servers = ServerGenerator.generateSideServersFor(continent, map)
      servers.size should (be >= 3 and be <= 6)
    }

    "generate only Side servers" in {
      val servers = ServerGenerator.generateSideServersFor(continent, map)
      all (servers.map(_.serverType)) shouldBe ServerType.Side
    }

    "generate servers on correct continent" in {
      val servers = ServerGenerator.generateSideServersFor(continent, map)
      val validPositions = map.tilesOf(continent).map(t => (t.x, t.y)).toSet

      all (servers.map(_.position)) should (be in validPositions)
    }

    "generate non-hacked servers by default" in {
      val servers = ServerGenerator.generateSideServersFor(continent, map)
      all (servers.map(_.hacked)) shouldBe false
    }

    "generate servers with unique positions" in {
      val servers = ServerGenerator.generateSideServersFor(continent, map)
      servers.map(_.position).distinct.size shouldBe servers.size
    }
  }
}