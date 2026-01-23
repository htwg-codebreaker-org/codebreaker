package de.htwg.codebreaker.model.player.laptop

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LaptopSpec extends AnyWordSpec with Matchers:

  "Laptop" should {

    "be created with all required fields" in {
      val hardware = LaptopHardware(
        cpu = 100,
        ram = 200,
        code = 50,
        kerne = 4,
        networkRange = 10
      )
      val tools = LaptopInstalledTools.empty
      val runningActions = List.empty[RunningLaptopAction]
      val runningSearch = None
      val cybersecurity = 75

      val laptop = Laptop(
        hardware = hardware,
        tools = tools,
        runningActions = runningActions,
        runningInternetSearch = runningSearch,
        cybersecurity = cybersecurity
      )

      laptop.hardware shouldBe hardware
      laptop.tools shouldBe tools
      laptop.runningActions shouldBe runningActions
      laptop.runningInternetSearch shouldBe runningSearch
      laptop.cybersecurity shouldBe 75
    }

    "support different hardware configurations" in {
      val lowEndHardware = LaptopHardware(50, 100, 25, 2, 5)
      val highEndHardware = LaptopHardware(500, 1000, 250, 16, 50)

      val lowEndLaptop = Laptop(lowEndHardware, LaptopInstalledTools.empty, List.empty, None, 50)
      val highEndLaptop = Laptop(highEndHardware, LaptopInstalledTools.empty, List.empty, None, 90)

      lowEndLaptop.hardware.cpu shouldBe 50
      highEndLaptop.hardware.cpu shouldBe 500
    }

    "support installed tools" in {
      val tool1 = LaptopTool("nmap", "Nmap", 10, 5, 3, "Network scanner", List.empty)
      val tool2 = LaptopTool("metasploit", "Metasploit", 20, 10, 5, "Exploit framework", List.empty)
      val tools = LaptopInstalledTools(List(tool1, tool2))

      val laptop = Laptop(
        LaptopHardware(100, 200, 50, 4, 10),
        tools,
        List.empty,
        None,
        75
      )

      laptop.tools.installedTools.size shouldBe 2
      laptop.tools.hasTool("nmap") shouldBe true
      laptop.tools.hasTool("metasploit") shouldBe true
      laptop.tools.hasTool("unknown") shouldBe false
    }

    "support running actions" in {
      val action = LaptopAction(
        id = "scan1",
        name = "Quick Scan",
        actionType = LaptopActionType.PortScan,
        durationRounds = 2,
        coreCost = 1,
        cpuCost = 10,
        ramCost = 20,
        description = "Scan a server",
        toolId = "nmap",
        Rewards = ActionRewards(5, 10, 0, 15)
      )
      val runningAction = RunningLaptopAction(action, 1, 3, "server1")

      val laptop = Laptop(
        LaptopHardware(100, 200, 50, 4, 10),
        LaptopInstalledTools.empty,
        List(runningAction),
        None,
        75
      )

      laptop.runningActions.size shouldBe 1
      laptop.runningActions.head.targetServer shouldBe "server1"
      laptop.runningActions.head.startRound shouldBe 1
      laptop.runningActions.head.completionRound shouldBe 3
    }

    "support running internet search" in {
      val tool = LaptopTool("tool1", "Tool 1", 5, 3, 2, "Found tool", List.empty)
      val search = RunningInternetSearch(
        startRound = 1,
        completionRound = 4,
        foundTools = List(tool)
      )

      val laptop = Laptop(
        LaptopHardware(100, 200, 50, 4, 10),
        LaptopInstalledTools.empty,
        List.empty,
        Some(search),
        75
      )

      laptop.runningInternetSearch shouldBe defined
      laptop.runningInternetSearch.get.startRound shouldBe 1
      laptop.runningInternetSearch.get.completionRound shouldBe 4
      laptop.runningInternetSearch.get.foundTools.size shouldBe 1
    }

    "support no running internet search" in {
      val laptop = Laptop(
        LaptopHardware(100, 200, 50, 4, 10),
        LaptopInstalledTools.empty,
        List.empty,
        None,
        75
      )

      laptop.runningInternetSearch shouldBe None
    }

    "support different cybersecurity levels" in {
      val lowSecurity = Laptop(LaptopHardware(100, 200, 50, 4, 10), LaptopInstalledTools.empty, List.empty, None, 10)
      val highSecurity = Laptop(LaptopHardware(100, 200, 50, 4, 10), LaptopInstalledTools.empty, List.empty, None, 100)

      lowSecurity.cybersecurity shouldBe 10
      highSecurity.cybersecurity shouldBe 100
    }

    "support copy with modifications" in {
      val original = Laptop(
        LaptopHardware(100, 200, 50, 4, 10),
        LaptopInstalledTools.empty,
        List.empty,
        None,
        75
      )

      val modified = original.copy(cybersecurity = 90)

      modified.cybersecurity shouldBe 90
      modified.hardware shouldBe original.hardware
      original.cybersecurity shouldBe 75
    }

    "support equality comparison" in {
      val hardware = LaptopHardware(100, 200, 50, 4, 10)
      val laptop1 = Laptop(hardware, LaptopInstalledTools.empty, List.empty, None, 75)
      val laptop2 = Laptop(hardware, LaptopInstalledTools.empty, List.empty, None, 75)
      val laptop3 = Laptop(hardware, LaptopInstalledTools.empty, List.empty, None, 80)

      laptop1 shouldBe laptop2
      laptop1 should not be laptop3
    }

    "support multiple running actions" in {
      val action1 = LaptopAction("a1", "Action 1", LaptopActionType.PortScan, 2, 1, 10, 20, "Desc1", "tool1", ActionRewards(5, 10, 0, 15))
      val action2 = LaptopAction("a2", "Action 2", LaptopActionType.BruteForce, 3, 2, 20, 40, "Desc2", "tool2", ActionRewards(10, 20, 5, 25))

      val running1 = RunningLaptopAction(action1, 1, 3, "server1")
      val running2 = RunningLaptopAction(action2, 2, 5, "server2")

      val laptop = Laptop(
        LaptopHardware(100, 200, 50, 4, 10),
        LaptopInstalledTools.empty,
        List(running1, running2),
        None,
        75
      )

      laptop.runningActions.size shouldBe 2
      laptop.runningActions.head.action.id shouldBe "a1"
      laptop.runningActions(1).action.id shouldBe "a2"
    }

    "support network range in hardware" in {
      val laptop = Laptop(
        LaptopHardware(100, 200, 50, 4, 25),
        LaptopInstalledTools.empty,
        List.empty,
        None,
        75
      )

      laptop.hardware.networkRange shouldBe 25
    }

    "support kerne (cores) in hardware" in {
      val laptop = Laptop(
        LaptopHardware(100, 200, 50, 8, 10),
        LaptopInstalledTools.empty,
        List.empty,
        None,
        75
      )

      laptop.hardware.kerne shouldBe 8
    }
  }
