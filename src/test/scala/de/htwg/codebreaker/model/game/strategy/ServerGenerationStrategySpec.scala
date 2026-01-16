package de.htwg.codebreaker.model.game.strategy

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class ServerGenerationStrategySpec extends AnyWordSpec with Matchers:

  val testMap = WorldMap.defaultMap

  "ServerGenerationStrategy trait" should {

    "define generateServers method" in {
      val strategy = new ServerGenerationStrategy {
        override def generateServers(map: WorldMap): List[Server] = 
          List(Server("Test", Tile(10, 10, Continent.Europe), 50, 100, 200, false, ServerType.Private))
      }

      val servers = strategy.generateServers(testMap)
      servers should not be null
      servers shouldBe a[List[?]]
    }

    "support generating multiple servers" in {
      val strategy = new ServerGenerationStrategy {
        override def generateServers(map: WorldMap): List[Server] = 
          (0 until 5).map(i => 
            Server(s"Server$i", Tile(i, i, Continent.Europe), 50, 100, 200, false, ServerType.Private)
          ).toList
      }

      val servers = strategy.generateServers(testMap)
      servers should have size 5
    }

    "receive world map parameter" in {
      val customMap = WorldMap(10, 10, Vector(Tile(0, 0, Continent.Ocean)))
      var receivedMap: WorldMap = null

      val strategy = new ServerGenerationStrategy {
        override def generateServers(map: WorldMap): List[Server] = 
          receivedMap = map
          Nil
      }

      strategy.generateServers(customMap)
      receivedMap shouldBe customMap
    }

    "support empty server generation" in {
      val strategy = new ServerGenerationStrategy {
        override def generateServers(map: WorldMap): List[Server] = Nil
      }

      val servers = strategy.generateServers(testMap)
      servers shouldBe empty
    }

    "allow strategies to create servers with different types" in {
      val strategy = new ServerGenerationStrategy {
        override def generateServers(map: WorldMap): List[Server] = 
          ServerType.values.zipWithIndex.map { case (serverType, i) =>
            Server(s"Server$i", Tile(i, i, Continent.Europe), 50, 100, 200, false, serverType)
          }.toList
      }

      val servers = strategy.generateServers(testMap)
      servers should have size ServerType.values.size
      servers.map(_.serverType).toSet shouldBe ServerType.values.toSet
    }

    "allow strategies to position servers on map" in {
      val strategy = new ServerGenerationStrategy {
        override def generateServers(map: WorldMap): List[Server] = 
          map.tiles.take(3).zipWithIndex.map { case (tile, i) =>
            Server(s"Server$i", tile, 50, 100, 200, false, ServerType.Private)
          }.toList
      }

      val servers = strategy.generateServers(testMap)
      servers should have size 3
      servers.map(_.tile) should contain allOf (testMap.tiles(0), testMap.tiles(1), testMap.tiles(2))
    }

    "support strategies with varying difficulty" in {
      val strategy = new ServerGenerationStrategy {
        override def generateServers(map: WorldMap): List[Server] = 
          List(
            Server("Easy", Tile(0, 0, Continent.Europe), 10, 50, 100, false, ServerType.Side),
            Server("Medium", Tile(1, 1, Continent.Europe), 50, 100, 200, false, ServerType.Firm),
            Server("Hard", Tile(2, 2, Continent.Europe), 90, 200, 400, false, ServerType.Military)
          )
      }

      val servers = strategy.generateServers(testMap)
      servers.map(_.difficulty) shouldBe List(10, 50, 90)
    }

    "support strategies with varying rewards" in {
      val strategy = new ServerGenerationStrategy {
        override def generateServers(map: WorldMap): List[Server] = 
          (1 to 3).map(i => 
            Server(s"S$i", Tile(i, i, Continent.Europe), 50, i * 100, i * 200, false, ServerType.Private)
          ).toList
      }

      val servers = strategy.generateServers(testMap)
      servers.map(_.rewardCpu) shouldBe List(100, 200, 300)
      servers.map(_.rewardRam) shouldBe List(200, 400, 600)
    }

    "create servers in unhacked state by default" in {
      val strategy = new ServerGenerationStrategy {
        override def generateServers(map: WorldMap): List[Server] = 
          List(Server("Test", Tile(0, 0, Continent.Europe), 50, 100, 200, false, ServerType.Bank))
      }

      val servers = strategy.generateServers(testMap)
      servers.foreach(_.hacked shouldBe false)
      servers.foreach(_.hackedBy shouldBe None)
      servers.foreach(_.claimedBy shouldBe None)
    }
  }
