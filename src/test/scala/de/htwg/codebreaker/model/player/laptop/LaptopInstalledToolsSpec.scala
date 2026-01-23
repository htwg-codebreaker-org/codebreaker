package de.htwg.codebreaker.model.player.laptop

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LaptopInstalledToolsSpec extends AnyWordSpec with Matchers:

  "LaptopInstalledTools" should {

    val tool1 = LaptopTool(
      id = "nmap",
      name = "Nmap",
      hackBonus = 10,
      stealthBonus = 5,
      speedBonus = 3,
      description = "Network scanner",
      availableActions = List.empty
    )

    val tool2 = LaptopTool(
      id = "wireshark",
      name = "Wireshark",
      hackBonus = 5,
      stealthBonus = 10,
      speedBonus = 2,
      description = "Packet analyzer",
      availableActions = List.empty
    )

    val tool3 = LaptopTool(
      id = "metasploit",
      name = "Metasploit",
      hackBonus = 20,
      stealthBonus = 0,
      speedBonus = 5,
      description = "Exploit framework",
      availableActions = List.empty
    )

    "be created with a list of tools" in {
      val tools = LaptopInstalledTools(List(tool1, tool2))

      tools.installedTools.length shouldBe 2
      tools.installedTools should contain(tool1)
      tools.installedTools should contain(tool2)
    }

    "be created with empty list" in {
      val tools = LaptopInstalledTools(List.empty)

      tools.installedTools shouldBe empty
    }

    "provide empty factory method" in {
      val tools = LaptopInstalledTools.empty

      tools.installedTools shouldBe empty
    }

    "return set of tool IDs" in {
      val tools = LaptopInstalledTools(List(tool1, tool2, tool3))

      val ids = tools.toolIds

      ids shouldBe Set("nmap", "wireshark", "metasploit")
    }

    "return empty set for no tools" in {
      val tools = LaptopInstalledTools.empty

      tools.toolIds shouldBe Set.empty
    }

    "return set with single ID for single tool" in {
      val tools = LaptopInstalledTools(List(tool1))

      tools.toolIds shouldBe Set("nmap")
    }

    "handle duplicate tool IDs in toolIds" in {
      val duplicateTool = tool1.copy()
      val tools = LaptopInstalledTools(List(tool1, duplicateTool, tool2))

      // Set should deduplicate
      tools.toolIds shouldBe Set("nmap", "wireshark")
    }

    "find tool by ID" in {
      val tools = LaptopInstalledTools(List(tool1, tool2, tool3))

      tools.getTool("nmap") shouldBe Some(tool1)
      tools.getTool("wireshark") shouldBe Some(tool2)
      tools.getTool("metasploit") shouldBe Some(tool3)
    }

    "return None for non-existent tool ID" in {
      val tools = LaptopInstalledTools(List(tool1, tool2))

      tools.getTool("nonexistent") shouldBe None
      tools.getTool("burpsuite") shouldBe None
    }

    "return None when searching in empty tools" in {
      val tools = LaptopInstalledTools.empty

      tools.getTool("nmap") shouldBe None
    }

    "return first tool when duplicate IDs exist" in {
      val tool1Modified = tool1.copy(hackBonus = 999)
      val tools = LaptopInstalledTools(List(tool1, tool1Modified))

      val found = tools.getTool("nmap")
      found shouldBe defined
      found.get.hackBonus shouldBe 10
    }

    "check if tool exists by ID" in {
      val tools = LaptopInstalledTools(List(tool1, tool2, tool3))

      tools.hasTool("nmap") shouldBe true
      tools.hasTool("wireshark") shouldBe true
      tools.hasTool("metasploit") shouldBe true
    }

    "return false for non-existent tool ID" in {
      val tools = LaptopInstalledTools(List(tool1, tool2))

      tools.hasTool("nonexistent") shouldBe false
      tools.hasTool("burpsuite") shouldBe false
    }

    "return false when checking empty tools" in {
      val tools = LaptopInstalledTools.empty

      tools.hasTool("nmap") shouldBe false
    }

    "return true when duplicate IDs exist" in {
      val tools = LaptopInstalledTools(List(tool1, tool1.copy()))

      tools.hasTool("nmap") shouldBe true
    }

    "support equality comparison" in {
      val tools1 = LaptopInstalledTools(List(tool1, tool2))
      val tools2 = LaptopInstalledTools(List(tool1, tool2))
      val tools3 = LaptopInstalledTools(List(tool1, tool3))

      tools1 shouldBe tools2
      tools1 should not be tools3
    }

    "support copy with modifications" in {
      val original = LaptopInstalledTools(List(tool1))
      val modified = original.copy(installedTools = List(tool1, tool2))

      modified.installedTools.length shouldBe 2
      original.installedTools.length shouldBe 1
    }

    "maintain order of tools" in {
      val tools = LaptopInstalledTools(List(tool3, tool1, tool2))

      tools.installedTools(0) shouldBe tool3
      tools.installedTools(1) shouldBe tool1
      tools.installedTools(2) shouldBe tool2
    }

    "handle single tool" in {
      val tools = LaptopInstalledTools(List(tool1))

      tools.installedTools.length shouldBe 1
      tools.toolIds shouldBe Set("nmap")
      tools.hasTool("nmap") shouldBe true
      tools.getTool("nmap") shouldBe Some(tool1)
    }

    "handle many tools" in {
      val manyTools = (1 to 100).map { i =>
        LaptopTool(
          s"tool$i",
          s"Tool $i",
          i,
          i * 2,
          i * 3,
          s"Description $i",
          List.empty
        )
      }.toList

      val tools = LaptopInstalledTools(manyTools)

      tools.installedTools.length shouldBe 100
      tools.toolIds.size shouldBe 100
      tools.hasTool("tool50") shouldBe true
      tools.getTool("tool50") shouldBe defined
    }

    "work with tools having same name but different IDs" in {
      val tool1a = LaptopTool("id1", "Same Name", 10, 5, 3, "Desc1", List.empty)
      val tool1b = LaptopTool("id2", "Same Name", 20, 10, 6, "Desc2", List.empty)

      val tools = LaptopInstalledTools(List(tool1a, tool1b))

      tools.toolIds shouldBe Set("id1", "id2")
      tools.hasTool("id1") shouldBe true
      tools.hasTool("id2") shouldBe true
      tools.getTool("id1") shouldBe Some(tool1a)
      tools.getTool("id2") shouldBe Some(tool1b)
    }

    "handle case-sensitive tool IDs" in {
      val tools = LaptopInstalledTools(List(tool1))

      tools.hasTool("nmap") shouldBe true
      tools.hasTool("NMAP") shouldBe false
      tools.hasTool("Nmap") shouldBe false
    }

    "return all installed tools via installedTools" in {
      val tools = LaptopInstalledTools(List(tool1, tool2, tool3))

      val allTools = tools.installedTools

      allTools.length shouldBe 3
      allTools should contain allOf (tool1, tool2, tool3)
    }

    "immutably store tools" in {
      val originalList = List(tool1, tool2)
      val tools = LaptopInstalledTools(originalList)

      tools.installedTools shouldBe originalList
      tools.installedTools.length shouldBe originalList.length
    }
  }

  "LaptopInstalledTools.empty" should {

    "create instance with empty list" in {
      val tools = LaptopInstalledTools.empty

      tools.installedTools shouldBe List.empty
    }

    "return empty set for toolIds" in {
      val tools = LaptopInstalledTools.empty

      tools.toolIds shouldBe Set.empty
    }

    "return None for getTool" in {
      val tools = LaptopInstalledTools.empty

      tools.getTool("anything") shouldBe None
    }

    "return false for hasTool" in {
      val tools = LaptopInstalledTools.empty

      tools.hasTool("anything") shouldBe false
    }

    "be equal to manually created empty instance" in {
      val empty1 = LaptopInstalledTools.empty
      val empty2 = LaptopInstalledTools(List.empty)

      empty1 shouldBe empty2
    }

    "support adding tools via copy" in {
      val empty = LaptopInstalledTools.empty
      val tool = LaptopTool("test", "Test", 10, 5, 3, "Test tool", List.empty)
      val withTool = empty.copy(installedTools = List(tool))

      withTool.installedTools.length shouldBe 1
      withTool.hasTool("test") shouldBe true
      empty.installedTools shouldBe List.empty
    }
  }

  "LaptopInstalledTools integration" should {

    "work correctly with all methods together" in {
      val tool = LaptopTool("combo", "Combo", 10, 5, 3, "Combined test", List.empty)
      val tools = LaptopInstalledTools(List(tool))

      tools.toolIds should contain("combo")
      tools.hasTool("combo") shouldBe true
      tools.getTool("combo") shouldBe Some(tool)
      tools.installedTools should contain(tool)
    }

    "handle adding and removing tools via copy" in {
      val initial = LaptopInstalledTools(List(
        LaptopTool("tool1", "Tool 1", 10, 5, 3, "First", List.empty)
      ))

      val withMore = initial.copy(installedTools = initial.installedTools :+
        LaptopTool("tool2", "Tool 2", 20, 10, 6, "Second", List.empty)
      )

      val removed = withMore.copy(installedTools =
        withMore.installedTools.filterNot(_.id == "tool1")
      )

      initial.installedTools.length shouldBe 1
      withMore.installedTools.length shouldBe 2
      removed.installedTools.length shouldBe 1
      removed.hasTool("tool1") shouldBe false
      removed.hasTool("tool2") shouldBe true
    }

    "maintain consistency between methods" in {
      val tool1 = LaptopTool("t1", "T1", 10, 5, 3, "D1", List.empty)
      val tool2 = LaptopTool("t2", "T2", 20, 10, 6, "D2", List.empty)
      val tools = LaptopInstalledTools(List(tool1, tool2))

      tools.toolIds.size shouldBe tools.installedTools.length
      tools.toolIds.foreach { id =>
        tools.hasTool(id) shouldBe true
        tools.getTool(id) shouldBe defined
      }
    }
  }
