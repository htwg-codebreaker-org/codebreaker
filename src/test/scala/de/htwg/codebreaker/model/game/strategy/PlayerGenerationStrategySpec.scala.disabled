package de.htwg.codebreaker.model.game.strategy

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model._

class PlayerGenerationStrategySpec extends AnyWordSpec with Matchers:

  val testMap = WorldMap.defaultMap
  val avoidTile = Tile(0, 0, Continent.Europe)

  "PlayerGenerationStrategy trait" should {

    "define generatePlayers method" in {
      val strategy = new PlayerGenerationStrategy {
        override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] = 
          List(Player(0, "Test", Tile(10, 10, Continent.Europe), 100, 100, 0, 0, 0, PlayerSkillTree(Set.empty), 30))
      }

      val players = strategy.generatePlayers(1, testMap, Nil)
      players should not be null
      players shouldBe a[List[?]]
    }

    "support generating multiple players" in {
      val strategy = new PlayerGenerationStrategy {
        override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] = 
          (0 until numPlayers).map(i => 
            Player(i, s"Player$i", Tile(i, i, Continent.Europe), 100, 100, 0, 0, 0, PlayerSkillTree(Set.empty), 30)
          ).toList
      }

      val players = strategy.generatePlayers(3, testMap, Nil)
      players should have size 3
      players.map(_.id) shouldBe List(0, 1, 2)
    }

    "respect numPlayers parameter" in {
      val strategy = new PlayerGenerationStrategy {
        override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] = 
          (0 until numPlayers).map(i => 
            Player(i, s"P$i", Tile(i*5, i*5, Continent.Asia), 100, 100, 0, 0, 0, PlayerSkillTree(Set.empty), 30)
          ).toList
      }

      strategy.generatePlayers(1, testMap, Nil) should have size 1
      strategy.generatePlayers(5, testMap, Nil) should have size 5
      strategy.generatePlayers(10, testMap, Nil) should have size 10
    }

    "receive world map parameter" in {
      val customMap = WorldMap(10, 10, Vector(Tile(0, 0, Continent.Ocean)))
      var receivedMap: WorldMap = null

      val strategy = new PlayerGenerationStrategy {
        override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] = 
          receivedMap = map
          Nil
      }

      strategy.generatePlayers(1, customMap, Nil)
      receivedMap shouldBe customMap
    }

    "receive avoidTiles parameter" in {
      val tilesToAvoid = List(avoidTile, Tile(1, 1, Continent.Asia))
      var receivedTiles: List[Tile] = null

      val strategy = new PlayerGenerationStrategy {
        override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] = 
          receivedTiles = avoidTiles
          Nil
      }

      strategy.generatePlayers(1, testMap, tilesToAvoid)
      receivedTiles shouldBe tilesToAvoid
    }

    "support empty player generation" in {
      val strategy = new PlayerGenerationStrategy {
        override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] = Nil
      }

      val players = strategy.generatePlayers(0, testMap, Nil)
      players shouldBe empty
    }

    "allow strategies to position players differently" in {
      val strategy1 = new PlayerGenerationStrategy {
        override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] = 
          List(Player(0, "P", Tile(0, 0, Continent.Europe), 100, 100, 0, 0, 0, PlayerSkillTree(Set.empty), 30))
      }

      val strategy2 = new PlayerGenerationStrategy {
        override def generatePlayers(numPlayers: Int, map: WorldMap, avoidTiles: List[Tile]): List[Player] = 
          List(Player(0, "P", Tile(10, 10, Continent.Asia), 100, 100, 0, 0, 0, PlayerSkillTree(Set.empty), 30))
      }

      val players1 = strategy1.generatePlayers(1, testMap, Nil)
      val players2 = strategy2.generatePlayers(1, testMap, Nil)
      
      players1.head.tile should not be players2.head.tile
    }
  }
