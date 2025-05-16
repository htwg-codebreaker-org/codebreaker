package de.htwg.codebreaker.model.game

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class GameModelSpec extends AnyWordSpec with Matchers {

  "GameModel" should {

    "initialize with explicit empty values" in {
      val model = GameModel(Nil, Nil, WorldMap.defaultMap)

      model.players shouldBe empty
      model.servers shouldBe empty
      model.worldMap should not be null
    }

    "store and return custom players, servers and map" in {
      val players = List(
        Player(0, "P1", Tile(0, 0, Continent.Europe), 10, 10, 10, 1, 0, 5)
      )

      val servers = List(
        Server("S1", Tile(1, 1, Continent.Asia), 20, 10, 15, false, ServerType.Cloud)
      )

      val map = WorldMap.defaultMap
      val model = GameModel(players, servers, map)

      model.players shouldBe players
      model.servers shouldBe servers
      model.worldMap shouldBe map
    }

    "allow updating via copy" in {
      val model = GameModel(Nil, Nil, WorldMap.defaultMap)

      val newPlayer = Player(1, "Updated", Tile(2, 2, Continent.Africa), 15, 15, 15, 2, 5, 10)
      val newServer = Server("S2", Tile(3, 3, Continent.Oceania), 25, 20, 25, false, ServerType.Bank)
      val newMap = WorldMap(1, 1, Vector(Tile(0, 0, Continent.Europe)))

      val updatedModel = model.copy(
        players = List(newPlayer),
        servers = List(newServer),
        worldMap = newMap
      )

      updatedModel.players.head.name shouldBe "Updated"
      updatedModel.servers.head.name shouldBe "S2"
      updatedModel.worldMap shouldBe newMap
    }
  }
}
