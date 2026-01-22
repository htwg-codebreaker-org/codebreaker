package de.htwg.codebreaker.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model.map.MapObject._
import de.htwg.codebreaker.model.map.{MapObject, Continent}
import de.htwg.codebreaker.model.server.ServerType

class MapObjectSpec extends AnyWordSpec with Matchers:

  "MapObject" should {

    "be a sealed trait" in {
      // Verify that all MapObject types can be created
      val obj: MapObject = PlayerOnTile(0)
      obj shouldBe a[MapObject]
    }
  }

  "PlayerOnTile" should {

    "be created with player index" in {
      val player = PlayerOnTile(1)
      player.index shouldBe 1
    }

    "be a MapObject" in {
      val player = PlayerOnTile(0)
      player shouldBe a[MapObject]
    }

    "support different player indices" in {
      val player1 = PlayerOnTile(0)
      val player2 = PlayerOnTile(5)
      
      player1 should not be player2
      player1.index shouldBe 0
      player2.index shouldBe 5
    }
  }

  "ServerOnTile" should {

    "be created with all required fields" in {
      val server = ServerOnTile(2, ServerType.Bank, Continent.Europe)
      
      server.index shouldBe 2
      server.serverType shouldBe ServerType.Bank
      server.continent shouldBe Continent.Europe
    }

    "be a MapObject" in {
      val server = ServerOnTile(0, ServerType.Private, Continent.Asia)
      server shouldBe a[MapObject]
    }

    "support different server types" in {
      ServerType.values.foreach { sType =>
        val server = ServerOnTile(0, sType, Continent.Africa)
        server.serverType shouldBe sType
      }
    }

    "support different continents" in {
      val serverEurope = ServerOnTile(0, ServerType.Firm, Continent.Europe)
      val serverAsia = ServerOnTile(0, ServerType.Firm, Continent.Asia)
      
      serverEurope.continent shouldBe Continent.Europe
      serverAsia.continent shouldBe Continent.Asia
    }
  }

  "PlayerAndServerTile" should {

    "be created with player, server indices and metadata" in {
      val combo = PlayerAndServerTile(1, 3, ServerType.Cloud, Continent.NorthAmerica)
      
      combo.playerIndex shouldBe 1
      combo.serverIndex shouldBe 3
      combo.serverType shouldBe ServerType.Cloud
      combo.continent shouldBe Continent.NorthAmerica
    }

    "be a MapObject" in {
      val combo = PlayerAndServerTile(0, 0, ServerType.Military, Continent.Europe)
      combo shouldBe a[MapObject]
    }

    "support different indices" in {
      val combo1 = PlayerAndServerTile(0, 1, ServerType.GKS, Continent.Asia)
      val combo2 = PlayerAndServerTile(2, 3, ServerType.GKS, Continent.Asia)
      
      combo1 should not be combo2
      combo1.playerIndex shouldBe 0
      combo2.playerIndex shouldBe 2
    }
  }

  "EmptyTile" should {

    "be created with continent" in {
      val empty = EmptyTile(Continent.Africa)
      empty.continent shouldBe Continent.Africa
    }

    "be a MapObject" in {
      val empty = EmptyTile(Continent.Ocean)
      empty shouldBe a[MapObject]
    }

    "support all continents" in {
      Continent.values.foreach { continent =>
        val empty = EmptyTile(continent)
        empty.continent shouldBe continent
      }
    }

    "differentiate between continents" in {
      val emptyAfrica = EmptyTile(Continent.Africa)
      val emptyEurope = EmptyTile(Continent.Europe)
      
      emptyAfrica should not be emptyEurope
    }
  }

  "MapObject subtypes" should {

    "all be distinct types" in {
      val player: MapObject = PlayerOnTile(0)
      val server: MapObject = ServerOnTile(0, ServerType.Side, Continent.Europe)
      val combo: MapObject = PlayerAndServerTile(0, 0, ServerType.Firm, Continent.Asia)
      val empty: MapObject = EmptyTile(Continent.Ocean)

      player should not be server
      player should not be combo
      player should not be empty
      server should not be combo
      server should not be empty
      combo should not be empty
    }
  }
