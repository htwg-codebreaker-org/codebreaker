package de.htwg.codebreaker.model.game.strategy

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class PlayerGeneratorSpec extends AnyWordSpec with Matchers {

  "PlayerGenerator" should {

    "generate the correct number of players on land tiles" in {
      val map = WorldMap.defaultMap
      val players = PlayerGenerator.generatePlayers(3, map, List.empty)

      players should have size 3
      all(players.map(_.tile.continent.isLand)) shouldBe true
      all(players.map(_.name.startsWith("Spieler"))) shouldBe true
    }

    "avoid placing players on the avoidTiles" in {
      val map = WorldMap.defaultMap
      val avoidTiles = map.tiles.take(5).toList
      val players = PlayerGenerator.generatePlayers(5, map, avoidTiles)

      avoidTiles.foreach { avoid =>
        all(players.map(_.tile)) should not be (avoid)
      }
    }
  }
}
