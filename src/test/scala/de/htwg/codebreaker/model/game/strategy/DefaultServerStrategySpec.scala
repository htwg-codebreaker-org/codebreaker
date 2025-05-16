package de.htwg.codebreaker.model.game.strategy

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class DefaultServerStrategySpec extends AnyWordSpec with Matchers {

  "DefaultServerStrategy" should {
    "generate a non-empty list of servers with valid tiles" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerStrategy.generateServers(map)

      servers should not be empty
      all(servers.map(_.tile)) should not be null
      servers.map(_.tile).distinct.size <= map.tiles.size shouldBe true
    }

    "generate servers only on land continents" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerStrategy.generateServers(map)

      all(servers.map(_.tile.continent)) should not be Continent.Ocean
    }
  }
}
