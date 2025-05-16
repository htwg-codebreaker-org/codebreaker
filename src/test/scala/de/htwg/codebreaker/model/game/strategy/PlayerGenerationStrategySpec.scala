package de.htwg.codebreaker.model.game.strategy

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class PlayerGenerationStrategySpec extends AnyWordSpec with Matchers {

  object DummyPlayerStrategy extends PlayerGenerationStrategy {
    override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] = {
      (0 until numPlayers).toList.map { i =>
        val tile = Tile(i, i, Continent.Europe)
        Player(
          id = i,
          name = s"Player$i",
          tile = tile,
          cpu = 10,
          ram = 10,
          code = 5,
          level = 1,
          xp = 0,
          cybersecurity = 3
        )
      }
    }
  }

  "PlayerGenerationStrategy" should {
    "generate the correct number of players with expected attributes" in {
      val map = WorldMap.defaultMap
      val avoid = List(Tile(0, 0, Continent.Europe))

      val players = DummyPlayerStrategy.generatePlayers(2, map, avoid)

      players should have size 2
      players.head.name shouldBe "Player0"
      all(players.map(_.tile.continent)) shouldBe Continent.Europe
      all(players.map(_.cpu)) shouldBe 10
    }
  }
}
