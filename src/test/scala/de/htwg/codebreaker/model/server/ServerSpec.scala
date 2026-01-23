package de.htwg.codebreaker.model.server

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.codebreaker.model.map.{Tile, Continent}

class ServerSpec extends AnyWordSpec with Matchers:

  val testTile = Tile(10, 15, Continent.Europe)

  "Server" should {

    "be created with all required fields" in {
      val server = Server(
        name = "TestServer",
        tile = testTile,
        difficulty = 50,
        rewardCpu = 100,
        rewardRam = 200,
        hacked = false,
        serverType = ServerType.Firm,
        hackedBy = None,
        claimedBy = None,
        cybersecurityLevel = 75,
        blockedUntilRound = None,
        installedRole = None
      )

      server.name shouldBe "TestServer"
      server.tile shouldBe testTile
      server.difficulty shouldBe 50
      server.rewardCpu shouldBe 100
      server.rewardRam shouldBe 200
      server.hacked shouldBe false
      server.serverType shouldBe ServerType.Firm
      server.hackedBy shouldBe None
      server.claimedBy shouldBe None
      server.cybersecurityLevel shouldBe 75
      server.blockedUntilRound shouldBe None
      server.installedRole shouldBe None
    }

    "support different server types" in {
      val sideServer = Server("Side", testTile, 10, 50, 100, false, ServerType.Side, None, None, 50, None, None)
      val firmServer = Server("Firm", testTile, 30, 100, 200, false, ServerType.Firm, None, None, 60, None, None)
      val cloudServer = Server("Cloud", testTile, 50, 200, 400, false, ServerType.Cloud, None, None, 70, None, None)
      val bankServer = Server("Bank", testTile, 70, 300, 600, false, ServerType.Bank, None, None, 80, None, None)
      val militaryServer = Server("Military", testTile, 90, 500, 1000, false, ServerType.Military, None, None, 95, None, None)
      val gksServer = Server("GKS", testTile, 100, 1000, 2000, false, ServerType.GKS, None, None, 100, None, None)
      val privateServer = Server("Private", testTile, 20, 75, 150, false, ServerType.Private, None, None, 55, None, None)

      sideServer.serverType shouldBe ServerType.Side
      firmServer.serverType shouldBe ServerType.Firm
      cloudServer.serverType shouldBe ServerType.Cloud
      bankServer.serverType shouldBe ServerType.Bank
      militaryServer.serverType shouldBe ServerType.Military
      gksServer.serverType shouldBe ServerType.GKS
      privateServer.serverType shouldBe ServerType.Private
    }

    "support different difficulty levels" in {
      val easyServer = Server("Easy", testTile, 10, 50, 100, false, ServerType.Side, None, None, 30, None, None)
      val mediumServer = Server("Medium", testTile, 50, 150, 300, false, ServerType.Firm, None, None, 60, None, None)
      val hardServer = Server("Hard", testTile, 90, 400, 800, false, ServerType.Military, None, None, 95, None, None)

      easyServer.difficulty shouldBe 10
      mediumServer.difficulty shouldBe 50
      hardServer.difficulty shouldBe 90
    }

    "support hacked status" in {
      val unhacked = Server("Unhacked", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)
      val hacked = Server("Hacked", testTile, 50, 100, 200, true, ServerType.Firm, Some(1), None, 75, None, None)

      unhacked.hacked shouldBe false
      unhacked.hackedBy shouldBe None
      hacked.hacked shouldBe true
      hacked.hackedBy shouldBe Some(1)
    }

    "support hackedBy player ID" in {
      val server = Server("Server", testTile, 50, 100, 200, true, ServerType.Firm, Some(42), None, 75, None, None)

      server.hackedBy shouldBe Some(42)
    }

    "support claimedBy player ID" in {
      val unclaimedServer = Server("Unclaimed", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)
      val claimedServer = Server("Claimed", testTile, 50, 100, 200, true, ServerType.Firm, Some(1), Some(1), 75, None, None)

      unclaimedServer.claimedBy shouldBe None
      claimedServer.claimedBy shouldBe Some(1)
    }

    "support different reward values" in {
      val lowReward = Server("Low", testTile, 20, 50, 100, false, ServerType.Side, None, None, 50, None, None)
      val highReward = Server("High", testTile, 80, 500, 1000, false, ServerType.Military, None, None, 90, None, None)

      lowReward.rewardCpu shouldBe 50
      lowReward.rewardRam shouldBe 100
      highReward.rewardCpu shouldBe 500
      highReward.rewardRam shouldBe 1000
    }

    "support different cybersecurity levels" in {
      val lowSecurity = Server("Low", testTile, 30, 100, 200, false, ServerType.Firm, None, None, 30, None, None)
      val highSecurity = Server("High", testTile, 70, 300, 600, false, ServerType.Military, None, None, 100, None, None)

      lowSecurity.cybersecurityLevel shouldBe 30
      highSecurity.cybersecurityLevel shouldBe 100
    }

    "support blockedUntilRound" in {
      val notBlocked = Server("NotBlocked", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)
      val blocked = Server("Blocked", testTile, 50, 100, 200, true, ServerType.Firm, Some(1), None, 75, Some(10), None)

      notBlocked.blockedUntilRound shouldBe None
      blocked.blockedUntilRound shouldBe Some(10)
    }

    "support installed server role" in {
      val role = InstalledServerRole(
        roleType = ServerRoleType.BitcoinMiner,
        installStartRound = 5,
        isActive = true,
        detectionRisk = 25,
        runningActions = List.empty,
        networkRange = 15
      )

      val server = Server("WithRole", testTile, 50, 100, 200, true, ServerType.Firm, Some(1), Some(1), 75, None, Some(role))

      server.installedRole shouldBe Some(role)
      server.installedRole.get.roleType shouldBe ServerRoleType.BitcoinMiner
      server.installedRole.get.installStartRound shouldBe 5
      server.installedRole.get.isActive shouldBe true
      server.installedRole.get.detectionRisk shouldBe 25
      server.installedRole.get.networkRange shouldBe 15
    }

    "support server without installed role" in {
      val server = Server("NoRole", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)

      server.installedRole shouldBe None
    }

    "support different server role types in installed role" in {
      val bitcoinRole = InstalledServerRole(ServerRoleType.BitcoinMiner, 1, true, 10, List.empty, 10)
      val darknetRole = InstalledServerRole(ServerRoleType.DarknetHost, 2, true, 15, List.empty, 20)
      val traderRole = InstalledServerRole(ServerRoleType.DataTrader, 3, true, 20, List.empty, 15)
      val botnetRole = InstalledServerRole(ServerRoleType.BotnetNode, 4, true, 25, List.empty, 30)

      bitcoinRole.roleType shouldBe ServerRoleType.BitcoinMiner
      darknetRole.roleType shouldBe ServerRoleType.DarknetHost
      traderRole.roleType shouldBe ServerRoleType.DataTrader
      botnetRole.roleType shouldBe ServerRoleType.BotnetNode
    }

    "support running role actions" in {
      val action = RunningRoleAction(
        actionId = "mine_bitcoin",
        startRound = 5,
        completionRound = 10,
        detectionIncrease = 10,
        expectedRewards = RoleActionReward(bitcoin = 100)
      )

      val role = InstalledServerRole(
        roleType = ServerRoleType.BitcoinMiner,
        installStartRound = 1,
        isActive = true,
        detectionRisk = 25,
        runningActions = List(action),
        networkRange = 15
      )

      val server = Server("WithActions", testTile, 50, 100, 200, true, ServerType.Firm, Some(1), Some(1), 75, None, Some(role))

      server.installedRole.get.runningActions.size shouldBe 1
      server.installedRole.get.runningActions.head.actionId shouldBe "mine_bitcoin"
      server.installedRole.get.runningActions.head.startRound shouldBe 5
      server.installedRole.get.runningActions.head.completionRound shouldBe 10
    }

    "support copy with modifications" in {
      val original = Server("Original", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)
      val modified = original.copy(hacked = true, hackedBy = Some(1))

      modified.hacked shouldBe true
      modified.hackedBy shouldBe Some(1)
      original.hacked shouldBe false
      original.hackedBy shouldBe None
    }

    "support equality comparison" in {
      val server1 = Server("Server", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)
      val server2 = Server("Server", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)
      val server3 = Server("Other", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)

      server1 shouldBe server2
      server1 should not be server3
    }
  }

  "Server companion object" should {

    "claim a server for a player" in {
      val server = Server("Server", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)
      val claimed = Server.claim(server, 1)

      claimed.claimedBy shouldBe Some(1)
      server.claimedBy shouldBe None
    }

    "unclaim a server" in {
      val server = Server("Server", testTile, 50, 100, 200, true, ServerType.Firm, Some(1), Some(1), 75, None, None)
      val unclaimed = Server.unclaim(server)

      unclaimed.claimedBy shouldBe None
      server.claimedBy shouldBe Some(1)
    }

    "claim server for different players" in {
      val server = Server("Server", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)
      val claimed1 = Server.claim(server, 1)
      val claimed2 = Server.claim(server, 2)

      claimed1.claimedBy shouldBe Some(1)
      claimed2.claimedBy shouldBe Some(2)
      server.claimedBy shouldBe None
    }

    "maintain immutability when claiming" in {
      val original = Server("Server", testTile, 50, 100, 200, false, ServerType.Firm, None, None, 75, None, None)
      val claimed = Server.claim(original, 1)

      original.claimedBy shouldBe None
      claimed.claimedBy shouldBe Some(1)
    }

    "maintain immutability when unclaiming" in {
      val original = Server("Server", testTile, 50, 100, 200, true, ServerType.Firm, Some(1), Some(1), 75, None, None)
      val unclaimed = Server.unclaim(original)

      original.claimedBy shouldBe Some(1)
      unclaimed.claimedBy shouldBe None
    }
  }

  "ServerBlueprint" should {

    "be created with all required fields" in {
      val blueprint = ServerBlueprint(
        name = "TestBlueprint",
        preferredPosition = (10, 20),
        serverType = ServerType.Firm,
        difficultyRange = (30, 50),
        rewardCpuRange = (100, 200),
        rewardRamRange = (200, 400)
      )

      blueprint.name shouldBe "TestBlueprint"
      blueprint.preferredPosition shouldBe (10, 20)
      blueprint.serverType shouldBe ServerType.Firm
      blueprint.difficultyRange shouldBe (30, 50)
      blueprint.rewardCpuRange shouldBe (100, 200)
      blueprint.rewardRamRange shouldBe (200, 400)
    }

    "support different positions" in {
      val blueprint = ServerBlueprint("Test", (5, 15), ServerType.Cloud, (40, 60), (150, 250), (300, 500))

      blueprint.preferredPosition._1 shouldBe 5
      blueprint.preferredPosition._2 shouldBe 15
    }

    "support different value ranges" in {
      val blueprint = ServerBlueprint("Test", (10, 10), ServerType.Bank, (70, 90), (300, 500), (600, 1000))

      blueprint.difficultyRange shouldBe (70, 90)
      blueprint.rewardCpuRange shouldBe (300, 500)
      blueprint.rewardRamRange shouldBe (600, 1000)
    }
  }

  "InstalledServerRole" should {

    "support inactive role type" in {
      val inactiveRole = InstalledServerRole(
        roleType = ServerRoleType.Inactive,
        installStartRound = 0,
        isActive = false,
        detectionRisk = 0,
        runningActions = List.empty,
        networkRange = 0
      )

      inactiveRole.roleType shouldBe ServerRoleType.Inactive
      inactiveRole.isActive shouldBe false
    }

    "support active role with high detection risk" in {
      val riskyRole = InstalledServerRole(
        roleType = ServerRoleType.BotnetNode,
        installStartRound = 5,
        isActive = true,
        detectionRisk = 90,
        runningActions = List.empty,
        networkRange = 25
      )

      riskyRole.detectionRisk shouldBe 90
      riskyRole.isActive shouldBe true
    }
  }

  "RoleActionReward" should {

    "be created with bitcoin reward" in {
      val reward = RoleActionReward(bitcoin = 100)

      reward.bitcoin shouldBe 100
      reward.code shouldBe 0
      reward.cpu shouldBe 0
      reward.ram shouldBe 0
    }

    "support all reward types" in {
      val reward = RoleActionReward(
        bitcoin = 50,
        code = 25,
        cpu = 100,
        ram = 200,
        networkRangeBonus = 5,
        cybersecurityDamage = 10
      )

      reward.bitcoin shouldBe 50
      reward.code shouldBe 25
      reward.cpu shouldBe 100
      reward.ram shouldBe 200
      reward.networkRangeBonus shouldBe 5
      reward.cybersecurityDamage shouldBe 10
    }

    "support default values" in {
      val reward = RoleActionReward()

      reward.bitcoin shouldBe 0
      reward.code shouldBe 0
      reward.cpu shouldBe 0
      reward.ram shouldBe 0
      reward.networkRangeBonus shouldBe 0
      reward.cybersecurityDamage shouldBe 0
    }

    "support equality comparison" in {
      val reward1 = RoleActionReward(bitcoin = 100, code = 50)
      val reward2 = RoleActionReward(bitcoin = 100, code = 50)
      val reward3 = RoleActionReward(bitcoin = 100, code = 60)

      reward1 shouldBe reward2
      reward1 should not be reward3
    }

    "support copy with modifications" in {
      val original = RoleActionReward(bitcoin = 100)
      val modified = original.copy(bitcoin = 200, code = 50)

      modified.bitcoin shouldBe 200
      modified.code shouldBe 50
      original.bitcoin shouldBe 100
      original.code shouldBe 0
    }
  }

  "RunningRoleAction" should {

    "be created with all required fields" in {
      val reward = RoleActionReward(bitcoin = 100, code = 50)
      val action = RunningRoleAction(
        actionId = "mine_bitcoin",
        startRound = 5,
        completionRound = 10,
        detectionIncrease = 15,
        expectedRewards = reward
      )

      action.actionId shouldBe "mine_bitcoin"
      action.startRound shouldBe 5
      action.completionRound shouldBe 10
      action.detectionIncrease shouldBe 15
      action.expectedRewards shouldBe reward
    }

    "support different action IDs" in {
      val reward = RoleActionReward()
      val action1 = RunningRoleAction("action1", 1, 5, 10, reward)
      val action2 = RunningRoleAction("action2", 2, 6, 20, reward)

      action1.actionId shouldBe "action1"
      action2.actionId shouldBe "action2"
    }

    "support different round ranges" in {
      val reward = RoleActionReward()
      val shortAction = RunningRoleAction("short", 1, 2, 5, reward)
      val longAction = RunningRoleAction("long", 1, 100, 5, reward)

      shortAction.completionRound - shortAction.startRound shouldBe 1
      longAction.completionRound - longAction.startRound shouldBe 99
    }

    "support different detection increases" in {
      val reward = RoleActionReward()
      val lowRisk = RunningRoleAction("low", 1, 5, 5, reward)
      val highRisk = RunningRoleAction("high", 1, 5, 50, reward)

      lowRisk.detectionIncrease shouldBe 5
      highRisk.detectionIncrease shouldBe 50
    }

    "support different reward types" in {
      val bitcoinReward = RoleActionReward(bitcoin = 100)
      val codeReward = RoleActionReward(code = 50)

      val bitcoinAction = RunningRoleAction("btc", 1, 5, 10, bitcoinReward)
      val codeAction = RunningRoleAction("code", 1, 5, 10, codeReward)

      bitcoinAction.expectedRewards.bitcoin shouldBe 100
      codeAction.expectedRewards.code shouldBe 50
    }

    "support equality comparison" in {
      val reward = RoleActionReward(bitcoin = 100)
      val action1 = RunningRoleAction("test", 1, 5, 10, reward)
      val action2 = RunningRoleAction("test", 1, 5, 10, reward)
      val action3 = RunningRoleAction("other", 1, 5, 10, reward)

      action1 shouldBe action2
      action1 should not be action3
    }

    "support copy with modifications" in {
      val reward = RoleActionReward(bitcoin = 100)
      val original = RunningRoleAction("action", 1, 5, 10, reward)
      val modified = original.copy(completionRound = 8, detectionIncrease = 20)

      modified.completionRound shouldBe 8
      modified.detectionIncrease shouldBe 20
      original.completionRound shouldBe 5
      original.detectionIncrease shouldBe 10
    }

    "calculate action duration correctly" in {
      val reward = RoleActionReward()
      val action = RunningRoleAction("test", 3, 8, 10, reward)

      val duration = action.completionRound - action.startRound
      duration shouldBe 5
    }

    "support zero detection increase" in {
      val reward = RoleActionReward()
      val action = RunningRoleAction("stealth", 1, 5, 0, reward)

      action.detectionIncrease shouldBe 0
    }

    "support high detection increase" in {
      val reward = RoleActionReward()
      val action = RunningRoleAction("risky", 1, 5, 100, reward)

      action.detectionIncrease shouldBe 100
    }

    "work with complex rewards" in {
      val complexReward = RoleActionReward(
        bitcoin = 50,
        code = 25,
        cpu = 100,
        ram = 200,
        networkRangeBonus = 5,
        cybersecurityDamage = 10
      )
      val action = RunningRoleAction("complex", 1, 10, 30, complexReward)

      action.expectedRewards.bitcoin shouldBe 50
      action.expectedRewards.code shouldBe 25
      action.expectedRewards.cpu shouldBe 100
      action.expectedRewards.ram shouldBe 200
      action.expectedRewards.networkRangeBonus shouldBe 5
      action.expectedRewards.cybersecurityDamage shouldBe 10
    }
  }
