package de.htwg.codebreaker.model.builder.strategy.generator.server

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model.map.{WorldMap, Tile, Continent}
import de.htwg.codebreaker.model.server.{ServerType, Server}

class DefaultServerGeneratorSpec extends AnyWordSpec with Matchers:

  "DefaultServerGenerator" should {

    "generate servers from the map" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      servers should not be empty
    }

    "generate fixed servers and side servers" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val fixedServers = servers.filter(_.serverType != ServerType.Side)
      val sideServers = servers.filter(_.serverType == ServerType.Side)

      fixedServers should not be empty
      sideServers should not be empty
    }

    "generate 11 fixed servers from blueprints" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val fixedServers = servers.filter(_.serverType != ServerType.Side)
      fixedServers.length shouldBe 11
    }

    "generate specific fixed servers with correct names" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val names = servers.map(_.name)
      names should contain("Pentagon")
      names should contain("Wall Street")
      names should contain("Silicon Valley")
      names should contain("Brussels")
      names should contain("Frankfurt Hub")
      names should contain("Moscow")
      names should contain("Beijing")
      names should contain("Tokyo Grid")
      names should contain("Sydney Core")
      names should contain("Cairo")
      names should contain("GKS")
    }

    "generate Pentagon as Military server" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val pentagon = servers.find(_.name == "Pentagon").get
      pentagon.serverType shouldBe ServerType.Military
    }

    "generate Wall Street as Bank server" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val wallStreet = servers.find(_.name == "Wall Street").get
      wallStreet.serverType shouldBe ServerType.Bank
    }

    "generate Silicon Valley as Cloud server" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val siliconValley = servers.find(_.name == "Silicon Valley").get
      siliconValley.serverType shouldBe ServerType.Cloud
    }

    "generate GKS as GKS server type" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val gks = servers.find(_.name == "GKS").get
      gks.serverType shouldBe ServerType.GKS
    }

    "generate Cairo as Firm server" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val cairo = servers.find(_.name == "Cairo").get
      cairo.serverType shouldBe ServerType.Firm
    }

    "place all servers on valid map tiles" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      servers.foreach { server =>
        map.tiles should contain(server.tile)
      }
    }

    "place all servers on land tiles" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      servers.foreach { server =>
        server.tile.continent.isLand shouldBe true
      }
    }

    "generate all servers as unhacked initially" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      servers.foreach { server =>
        server.hacked shouldBe false
        server.hackedBy shouldBe None
        server.claimedBy shouldBe None
      }
    }

    "generate all servers with no installed role initially" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      servers.foreach { server =>
        server.installedRole shouldBe None
      }
    }

    "generate all servers with zero cybersecurity level" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      servers.foreach { server =>
        server.cybersecurityLevel shouldBe 0
      }
    }

    "generate all servers with no blocked rounds" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      servers.foreach { server =>
        server.blockedUntilRound shouldBe None
      }
    }

    "generate side servers with difficulty in range 20-50" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val sideServers = servers.filter(_.serverType == ServerType.Side)
      sideServers.foreach { server =>
        server.difficulty should be >= 20
        server.difficulty should be <= 50
      }
    }

    "generate side servers with CPU rewards in range 10-20" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val sideServers = servers.filter(_.serverType == ServerType.Side)
      sideServers.foreach { server =>
        server.rewardCpu should be >= 10
        server.rewardCpu should be <= 20
      }
    }

    "generate side servers with RAM rewards in range 10-20" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val sideServers = servers.filter(_.serverType == ServerType.Side)
      sideServers.foreach { server =>
        server.rewardRam should be >= 10
        server.rewardRam should be <= 20
      }
    }

    "generate side servers with names containing continent codes" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val sideServers = servers.filter(_.serverType == ServerType.Side)
      sideServers.foreach { server =>
        server.name should startWith("Nebenserver-")
      }
    }

    "generate side servers for each land continent" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val sideServers = servers.filter(_.serverType == ServerType.Side)
      val continents = sideServers.map(_.tile.continent).distinct

      continents should not be empty
      continents.foreach { continent =>
        continent.isLand shouldBe true
      }
    }

    "generate Pentagon with difficulty in range 85-90" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val pentagon = servers.find(_.name == "Pentagon").get
      pentagon.difficulty should be >= 85
      pentagon.difficulty should be <= 90
    }

    "generate GKS with difficulty in range 90-100" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val gks = servers.find(_.name == "GKS").get
      gks.difficulty should be >= 90
      gks.difficulty should be <= 100
    }

    "generate GKS with zero rewards" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val gks = servers.find(_.name == "GKS").get
      gks.rewardCpu shouldBe 0
      gks.rewardRam shouldBe 0
    }

    "place Pentagon at position (24, 14)" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val pentagon = servers.find(_.name == "Pentagon").get
      pentagon.tile.x shouldBe 24
      pentagon.tile.y shouldBe 14
    }

    "place GKS at position (11, 8)" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val gks = servers.find(_.name == "GKS").get
      gks.tile.x shouldBe 11
      gks.tile.y shouldBe 8
    }
  }

  "DefaultServerGenerator.distance" should {

    "calculate Manhattan distance correctly" in {
      val tile1 = Tile(0, 0, Continent.Europe)
      val tile2 = Tile(3, 4, Continent.Europe)

      DefaultServerGenerator.distance(tile1, tile2) shouldBe 7
    }

    "return zero for same tile" in {
      val tile = Tile(5, 5, Continent.Asia)

      DefaultServerGenerator.distance(tile, tile) shouldBe 0
    }

    "calculate distance horizontally" in {
      val tile1 = Tile(0, 5, Continent.Europe)
      val tile2 = Tile(10, 5, Continent.Europe)

      DefaultServerGenerator.distance(tile1, tile2) shouldBe 10
    }

    "calculate distance vertically" in {
      val tile1 = Tile(5, 0, Continent.Europe)
      val tile2 = Tile(5, 8, Continent.Europe)

      DefaultServerGenerator.distance(tile1, tile2) shouldBe 8
    }

    "calculate distance for negative differences" in {
      val tile1 = Tile(10, 10, Continent.Europe)
      val tile2 = Tile(3, 2, Continent.Europe)

      DefaultServerGenerator.distance(tile1, tile2) shouldBe 15
    }

    "be symmetric" in {
      val tile1 = Tile(5, 7, Continent.Europe)
      val tile2 = Tile(12, 15, Continent.Asia)

      val dist1 = DefaultServerGenerator.distance(tile1, tile2)
      val dist2 = DefaultServerGenerator.distance(tile2, tile1)

      dist1 shouldBe dist2
    }
  }

  "DefaultServerGenerator.rngIn" should {

    "return value within range" in {
      val results = (1 to 100).map(_ => DefaultServerGenerator.rngIn((10, 20)))

      results.foreach { value =>
        value should be >= 10
        value should be <= 20
      }
    }

    "return min value when min equals max" in {
      val result = DefaultServerGenerator.rngIn((42, 42))

      result shouldBe 42
    }

    "handle range with difference of 1" in {
      val results = (1 to 100).map(_ => DefaultServerGenerator.rngIn((5, 6)))

      results.foreach { value =>
        value should (be(5) or be(6))
      }
    }

    "handle large ranges" in {
      val results = (1 to 100).map(_ => DefaultServerGenerator.rngIn((0, 100)))

      results.foreach { value =>
        value should be >= 0
        value should be <= 100
      }
    }

    "generate different values for different calls" in {
      val results = (1 to 50).map(_ => DefaultServerGenerator.rngIn((1, 100))).toSet

      results.size should be > 1
    }
  }

  "DefaultServerGenerator.pickNonCloseTiles" should {

    "pick tiles with minimum distance constraint" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(5, 0, Continent.Europe),
        Tile(10, 0, Continent.Europe),
        Tile(15, 0, Continent.Europe),
        Tile(20, 0, Continent.Europe)
      )

      val picked = DefaultServerGenerator.pickNonCloseTiles(tiles, 3, 5)

      picked.length should be <= 3
      for {
        i <- picked.indices
        j <- picked.indices
        if i != j
      } {
        DefaultServerGenerator.distance(picked(i), picked(j)) should be >= 5
      }
    }

    "return empty vector when count is 0" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 1, Continent.Europe)
      )

      val picked = DefaultServerGenerator.pickNonCloseTiles(tiles, 0, 2)

      picked shouldBe empty
    }

    "avoid specified tiles" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(5, 0, Continent.Europe),
        Tile(10, 0, Continent.Europe)
      )
      val avoidTile = Tile(5, 0, Continent.Europe)

      val picked = DefaultServerGenerator.pickNonCloseTiles(tiles, 3, 3, List(avoidTile))

      picked should not contain avoidTile
    }

    "respect distance constraint with avoided tiles" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(5, 0, Continent.Europe),
        Tile(10, 0, Continent.Europe),
        Tile(15, 0, Continent.Europe)
      )
      val avoidTile = Tile(7, 0, Continent.Europe)

      val picked = DefaultServerGenerator.pickNonCloseTiles(tiles, 2, 5, List(avoidTile))

      picked.foreach { tile =>
        DefaultServerGenerator.distance(tile, avoidTile) should be >= 5
      }
    }

    "handle case when no tiles satisfy distance constraint" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 0, Continent.Europe),
        Tile(2, 0, Continent.Europe)
      )

      val picked = DefaultServerGenerator.pickNonCloseTiles(tiles, 3, 10)

      picked.length should be <= 3
    }

    "pick single tile when count is 1" in {
      val tiles = Vector(
        Tile(0, 0, Continent.Europe),
        Tile(1, 1, Continent.Europe)
      )

      val picked = DefaultServerGenerator.pickNonCloseTiles(tiles, 1, 5)

      picked.length shouldBe 1
    }

    "not exceed requested count" in {
      val tiles = Vector.tabulate(20)(i => Tile(i * 10, 0, Continent.Europe))

      val picked = DefaultServerGenerator.pickNonCloseTiles(tiles, 5, 2)

      picked.length should be <= 5
    }
  }

  "DefaultServerGenerator integration" should {

    "generate consistent number of servers across multiple runs for same map" in {
      val map = WorldMap.defaultMap

      val count1 = DefaultServerGenerator.generateServers(map).length
      val count2 = DefaultServerGenerator.generateServers(map).length

      // Fixed servers count should be same, side servers might vary
      val fixedCount = 11
      count1 should be >= fixedCount
      count2 should be >= fixedCount
    }

    "generate all fixed servers at correct positions every time" in {
      val map = WorldMap.defaultMap

      val servers1 = DefaultServerGenerator.generateServers(map)
      val servers2 = DefaultServerGenerator.generateServers(map)

      val fixed1 = servers1.filter(_.serverType != ServerType.Side).sortBy(_.name)
      val fixed2 = servers2.filter(_.serverType != ServerType.Side).sortBy(_.name)

      fixed1.zip(fixed2).foreach { case (s1, s2) =>
        s1.name shouldBe s2.name
        s1.tile shouldBe s2.tile
        s1.serverType shouldBe s2.serverType
      }
    }

    "not place multiple servers on the same tile" in {
      val map = WorldMap.defaultMap
      val servers = DefaultServerGenerator.generateServers(map)

      val tiles = servers.map(_.tile)
      tiles.distinct.length shouldBe tiles.length
    }
  }
