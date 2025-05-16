package de.htwg.codebreaker.model.game.strategy

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class RandomPlayerGeneratorSpec extends AnyWordSpec with Matchers {

  "RandomPlayerGenerator" should {
    "generate players via PlayerGenerator and respect avoidTiles" in {
      val map = WorldMap.defaultMap
      val avoid = map.tiles.take(5).toList

      val players = RandomPlayerGenerator.generatePlayers(5, map, avoid)

      players should have size 5
      all(players.map(_.tile.continent.isLand)) shouldBe true

      avoid.foreach { avoidTile =>
        all(players.map(_.tile)) should not be (avoidTile)
      }
    }
  }
}
