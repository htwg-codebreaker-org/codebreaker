package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class GameModelSpec extends AnyWordSpec with Matchers {

  "GameModel" should {

    "initialize with default values" in {
      val model = GameModel()
      model.players shouldBe empty
      model.servers shouldBe empty
      model.worldMap shouldBe WorldMap.defaultMap
    }

    "store and return custom players, servers and map" in {
      val players = List(Player(0, "P1", Tile(0, 0, Continent.Europe), 10, 10, 10, 1, 0, 5))
      val servers = List(Server("S1", Tile(1, 1, Continent.Asia), 20, 10, 15, false, ServerType.Cloud))
      val map = WorldMap(1, 1, Vector(Tile(0, 0, Continent.Europe)))

      val model = GameModel(players, servers, map)

      model.players shouldBe players
      model.servers shouldBe servers
      model.worldMap shouldBe map
    }

    "allow mutation of players and servers" in {
      val model = GameModel()
      val newPlayer = Player(1, "Test", Tile(1, 1, Continent.Africa), 5, 5, 5, 0, 0, 0)
      val newServer = Server("X", Tile(0, 0, Continent.Asia), 10, 10, 10, false, ServerType.Bank)

      model.players = List(newPlayer)
      model.servers = List(newServer)

      model.players should contain (newPlayer)
      model.servers should contain (newServer)
    }

    "support equality and hashCode" in {
      val map = WorldMap.defaultMap
      val m1 = GameModel(Nil, Nil, map)
      val m2 = GameModel(Nil, Nil, map)

      m1 shouldBe m2
      m1.hashCode() shouldBe m2.hashCode()
    }
  }
}
