// ==================== DEFAULT IMPLEMENTATION ====================

package de.htwg.codebreaker.model.builder.strategy.generator.player.laptop

import de.htwg.codebreaker.model.builder.strategy.LaptopToolGenerationStrategy
import de.htwg.codebreaker.model.player.laptop._

object DefaultLaptopToolGenerator extends LaptopToolGenerationStrategy {

  override def generateLaptopTools(): List[LaptopTool] = List(
    createNmap(),
    createMetasploit(),
    createWireshark(),
    createJohnTheRipper(),
    createBurpSuite(),
    createSQLMap()
  )

  // ==================== TOOL DEFINITIONS ====================

  private def createNmap(): LaptopTool = LaptopTool(
    id = "nmap",
    name = "Nmap",
    description = "Network Scanner für Port-Scans",
    hackBonus = 5,
    stealthBonus = 10,
    speedBonus = 0,
    availableActions = List(
      LaptopAction(
        id = "nmap_quick_scan",
        name = "Quick Scan",
        actionType = LaptopActionType.PortScan,
        durationRounds = 1,
        coreCost = 1,
        cpuCost = 5,
        ramCost = 3,
        description = "Schneller Port-Scan",
        toolId = "nmap",
        Rewards = ActionRewards(
          cpuGained = 0,
          ramGained = 0,
          codeGained = 0,
          xpGained = 10
        )
      ),
      LaptopAction(
        id = "nmap_deep_scan",
        name = "Deep Scan",
        actionType = LaptopActionType.PortScan,
        durationRounds = 3,
        coreCost = 2,
        cpuCost = 15,
        ramCost = 10,
        description = "Gründlicher Port-Scan mit Schwachstellen-Analyse",
        toolId = "nmap",
        Rewards = ActionRewards(
          cpuGained = 0,
          ramGained = 0,
          codeGained = 0,
          xpGained = 20
        )
      )
    )
  )

  private def createMetasploit(): LaptopTool = LaptopTool(
    id = "metasploit",
    name = "Metasploit Framework",
    description = "Exploit-Framework für Server-Angriffe",
    hackBonus = 15,
    stealthBonus = 0,
    speedBonus = 1,
    availableActions = List(
      LaptopAction(
        id = "metasploit_quick_exploit",
        name = "Quick Exploit",
        actionType = LaptopActionType.Exploit,
        durationRounds = 1,
        coreCost = 2,
        cpuCost = 15,
        ramCost = 10,
        description = "Schneller Exploit-Angriff",
        toolId = "metasploit",
        Rewards = ActionRewards(
          cpuGained = 0,
          ramGained = 0,
          codeGained = 0,
          xpGained = 15
        )
      ),
      LaptopAction(
        id = "metasploit_advanced_exploit",
        name = "Advanced Exploit",
        actionType = LaptopActionType.Exploit,
        durationRounds = 3,
        coreCost = 3,
        cpuCost = 30,
        ramCost = 25,
        description = "Komplexer Exploit mit höherer Erfolgsrate",
        toolId = "metasploit",
        Rewards = ActionRewards(
          cpuGained = 0,
          ramGained = 0,
          codeGained = 0,
          xpGained = 25
        )
      )
    )
  )

  private def createWireshark(): LaptopTool = LaptopTool(
    id = "wireshark",
    name = "Wireshark",
    description = "Paket-Sniffer für Netzwerk-Analyse",
    hackBonus = 0,
    stealthBonus = 20,
    speedBonus = 0,
    availableActions = List(
      LaptopAction(
        id = "wireshark_data_extraction",
        name = "Data Extraction",
        actionType = LaptopActionType.DataExtraction,
        durationRounds = 2,
        coreCost = 1,
        cpuCost = 10,
        ramCost = 15,
        description = "Extrahiert Daten von einem Server",
        toolId = "wireshark",
        Rewards = ActionRewards(
          cpuGained = 0,
          ramGained = 0,
          codeGained = 0,
          xpGained = 15
        )
      )
    )
  )

  private def createJohnTheRipper(): LaptopTool = LaptopTool(
    id = "john_the_ripper",
    name = "John the Ripper",
    description = "Password Cracker",
    hackBonus = 10,
    stealthBonus = -10,
    speedBonus = 0,
    availableActions = List(
      LaptopAction(
        id = "john_bruteforce",
        name = "BruteForce Attack",
        actionType = LaptopActionType.BruteForce,
        durationRounds = 2,
        coreCost = 1,
        cpuCost = 10,
        ramCost = 5,
        description = "Klassischer Brute Force Angriff",
        toolId = "john_the_ripper",
        Rewards = ActionRewards(
          cpuGained = 0,
          ramGained = 0,
          codeGained = 0,
          xpGained = 15
        )
      ),
      LaptopAction(
        id = "john_dictionary",
        name = "Dictionary Attack",
        actionType = LaptopActionType.BruteForce,
        durationRounds = 1,
        coreCost = 2,
        cpuCost = 20,
        ramCost = 10,
        description = "Dictionary-basierter Angriff (schneller aber weniger effektiv)",
        toolId = "john_the_ripper",
        Rewards = ActionRewards(
          cpuGained = 0,
          ramGained = 0,
          codeGained = 0,
          xpGained = 15
        )
      )
    )
  )

  private def createBurpSuite(): LaptopTool = LaptopTool(
    id = "burpsuite",
    name = "Burp Suite",
    description = "Web Application Security Testing",
    hackBonus = 8,
    stealthBonus = 5,
    speedBonus = 0,
    availableActions = List(
      LaptopAction(
        id = "burp_web_scan",
        name = "Web Vulnerability Scan",
        actionType = LaptopActionType.PortScan,
        durationRounds = 2,
        coreCost = 2,
        cpuCost = 12,
        ramCost = 8,
        description = "Scannt Web-Anwendungen nach Schwachstellen",
        toolId = "burpsuite",
        Rewards = ActionRewards(
          cpuGained = 0,
          ramGained = 0,
          codeGained = 0,
          xpGained = 10
        )
      )
    )
  )

  private def createSQLMap(): LaptopTool = LaptopTool(
    id = "sqlmap",
    name = "SQLMap",
    description = "Automatisches SQL Injection Tool",
    hackBonus = 12,
    stealthBonus = -5,
    speedBonus = 1,
    availableActions = List(
      LaptopAction(
        id = "sqlmap_injection",
        name = "SQL Injection Attack",
        actionType = LaptopActionType.Exploit,
        durationRounds = 2,
        coreCost = 2,
        cpuCost = 18,
        ramCost = 12,
        description = "Automatisierter SQL Injection Angriff",
        toolId = "sqlmap",
        Rewards = ActionRewards(
          cpuGained = 0,
          ramGained = 0,
          codeGained = 0,
          xpGained = 20
        )
      )
    )
  )
}