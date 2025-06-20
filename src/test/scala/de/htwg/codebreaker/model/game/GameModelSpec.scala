package de.htwg.codebreaker.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model.game.GameModel


class GameModelSpec extends AnyWordSpec with Matchers {

  "A GameModel" should {

    val tile = Tile(0, 0, Continent.Europe)
    val player = Player(0, "Tester", tile, 1, 1, 1, 1, 0, 0)
    val server = Server("S1", tile, 10, 2, 2, false, ServerType.Firm)
    val map = WorldMap(1, 1, Vector(tile))

    val model = GameModel(List(player), List(server), map)

    "store and return players" in {
      model.players should have size 1
      model.players.head.name shouldBe "Tester"
    }

    "store and return servers" in {
      model.servers should have size 1
      model.servers.head.name shouldBe "S1"
    }

    "store and return the world map" in {
      model.worldMap shouldBe map
    }

    "provide combined map data" in {
      val data = model.worldMap.getMapData(model.players, model.servers)
      data should have length 1
      data.head should have length 1
    }
  }
}
