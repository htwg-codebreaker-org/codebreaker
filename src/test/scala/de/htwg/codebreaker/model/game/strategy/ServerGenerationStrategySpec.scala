package de.htwg.codebreaker.model.game.strategy

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class ServerGenerationStrategySpec extends AnyWordSpec with Matchers {

  object DummyServerStrategy extends ServerGenerationStrategy {
    override def generateServers(map: WorldMap): List[Server] = {
      List(
        Server(
          name = "Dummy",
          tile = Tile(0, 0, Continent.Europe),
          difficulty = 10,
          rewardCpu = 5,
          rewardRam = 5,
          hacked = false,
          serverType = ServerType.Cloud
        )
      )
    }
  }

  "ServerGenerationStrategy" should {
    "generate servers based on the strategy implementation" in {
      val map = WorldMap.defaultMap
      val servers = DummyServerStrategy.generateServers(map)

      servers should have size 1
      servers.head.name shouldBe "Dummy"
      servers.head.tile.continent shouldBe Continent.Europe
      servers.head.hacked shouldBe false
    }
  }
}
