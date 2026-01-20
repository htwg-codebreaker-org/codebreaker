// src/main/scala/de/htwg/codebreaker/model/game/strategy/server/ServerRoleGenerator.scala
package de.htwg.codebreaker.model.game.strategy.server

import de.htwg.codebreaker.model.server._

/** 
 * ServerRoleGenerator kümmert sich um die Erzeugung von Server-Role Blueprints:
 * - Role Blueprints: Definiert welche Roles verfügbar sind
 * - Action Blueprints: Definiert welche Actions pro Role verfügbar sind
 */
object ServerRoleGenerator:

  // ═══════════════════════════════════════════════════════
  // ROLE BLUEPRINTS
  // ═══════════════════════════════════════════════════════

  /**
   * Erzeugt alle verfügbaren Server-Role Blueprints.
   */
  def generateRoleBlueprints: List[ServerRoleBlueprint] =
    roleBlueprints

  /**
   * Erzeugt alle verfügbaren Role-Action Blueprints.
   */
  def generateActionBlueprints: List[RoleActionBlueprint] =
    actionBlueprints

  // --------------------------------------------
  // Vordefinierte Role Blueprints
  // --------------------------------------------

  private val roleBlueprints: List[ServerRoleBlueprint] = List(
    ServerRoleBlueprint(
      roleType = ServerRoleType.BitcoinMiner,
      name = "Bitcoin Miner",
      setupDurationRounds = 2,
      baseDetectionRisk = 20,
      availableActionIds = List("mine_bitcoin", "optimize_mining", "pool_connect"),
      description = "Generiert Bitcoin durch Mining",
      networkRange = 0
    ),
    ServerRoleBlueprint(
      roleType = ServerRoleType.DarknetHost,
      name = "Darknet Host",
      setupDurationRounds = 3,
      baseDetectionRisk = 45,
      availableActionIds = List("host_marketplace", "run_hidden_service", "launder_money"),
      description = "Hostet illegale Services im Darknet",
      networkRange = 0
    ),
    ServerRoleBlueprint(
      roleType = ServerRoleType.DataTrader,
      name = "Data Trader",
      setupDurationRounds = 2,
      baseDetectionRisk = 30,
      availableActionIds = List("extract_data", "sell_data", "analyze_patterns"),
      description = "Verkauft gestohlene Daten",
      networkRange = 0
    ),
    ServerRoleBlueprint(
      roleType = ServerRoleType.BotnetNode,
      name = "Botnet Node",
      setupDurationRounds = 4,
      baseDetectionRisk = 60,
      availableActionIds = List("launch_ddos", "spread_malware", "command_bots"),
      description = "Command & Control für Botnet",
      networkRange = 10
    ),
    ServerRoleBlueprint(
      roleType = ServerRoleType.Inactive,
      name = "Inactive",
      setupDurationRounds = 0,
      baseDetectionRisk = 5,
      availableActionIds = List(),
      description = "Server ist claimed, aber nicht aktiv",
      networkRange = 0
    )
  )

  // --------------------------------------------
  // Vordefinierte Action Blueprints
  // --------------------------------------------

  private val actionBlueprints: List[RoleActionBlueprint] = List(
    
    // ─────── BITCOIN MINER ACTIONS ───────
    RoleActionBlueprint(
      id = "mine_bitcoin",
      name = "Mine Bitcoin",
      roleType = ServerRoleType.BitcoinMiner,
      durationRounds = 2,
      detectionRiskIncrease = 10,
      rewards = RoleActionReward(bitcoin = 50),
      requirements = RoleActionRequirements(minCpu = 20),
      description = "Standard Bitcoin Mining über 2 Runden"
    ),
    RoleActionBlueprint(
      id = "optimize_mining",
      name = "Optimize Mining",
      roleType = ServerRoleType.BitcoinMiner,
      durationRounds = 3,
      detectionRiskIncrease = 5,
      rewards = RoleActionReward(bitcoin = 100),
      requirements = RoleActionRequirements(minCpu = 40, minCode = 20),
      description = "Optimiertes Mining für höhere Ausbeute"
    ),
    RoleActionBlueprint(
      id = "pool_connect",
      name = "Connect to Pool",
      roleType = ServerRoleType.BitcoinMiner,
      durationRounds = 1,
      detectionRiskIncrease = 15,
      rewards = RoleActionReward(bitcoin = 30),
      requirements = RoleActionRequirements(),
      description = "Schnelles Mining durch Pool-Verbindung"
    ),

    // ─────── DARKNET HOST ACTIONS ───────
    RoleActionBlueprint(
      id = "host_marketplace",
      name = "Host Marketplace",
      roleType = ServerRoleType.DarknetHost,
      durationRounds = 3,
      detectionRiskIncrease = 25,
      rewards = RoleActionReward(bitcoin = 80),
      requirements = RoleActionRequirements(minRam = 30),
      description = "Hoste illegalen Marktplatz für 3 Runden"
    ),
    RoleActionBlueprint(
      id = "run_hidden_service",
      name = "Run Hidden Service",
      roleType = ServerRoleType.DarknetHost,
      durationRounds = 2,
      detectionRiskIncrease = 20,
      rewards = RoleActionReward(bitcoin = 50),
      requirements = RoleActionRequirements(minRam = 20),
      description = "Betreibe versteckten Onion-Service"
    ),
    RoleActionBlueprint(
      id = "launder_money",
      name = "Launder Money",
      roleType = ServerRoleType.DarknetHost,
      durationRounds = 4,
      detectionRiskIncrease = 35,
      rewards = RoleActionReward(bitcoin = 150, ram = 100),
      requirements = RoleActionRequirements(minCode = 30),
      description = "Geldwäsche über Mixer-Services"
    ),

    // ─────── DATA TRADER ACTIONS ───────
    RoleActionBlueprint(
      id = "extract_data",
      name = "Extract Data",
      roleType = ServerRoleType.DataTrader,
      durationRounds = 2,
      detectionRiskIncrease = 15,
      rewards = RoleActionReward(code = 40),
      requirements = RoleActionRequirements(minCpu = 15),
      description = "Extrahiere wertvolle Daten vom Server"
    ),
    RoleActionBlueprint(
      id = "sell_data",
      name = "Sell Data",
      roleType = ServerRoleType.DataTrader,
      durationRounds = 1,
      detectionRiskIncrease = 10,
      rewards = RoleActionReward(bitcoin = 60),
      requirements = RoleActionRequirements(minCode = 20),
      description = "Verkaufe gestohlene Daten auf dem Schwarzmarkt"
    ),
    RoleActionBlueprint(
      id = "analyze_patterns",
      name = "Analyze Patterns",
      roleType = ServerRoleType.DataTrader,
      durationRounds = 3,
      detectionRiskIncrease = 5,
      rewards = RoleActionReward(code = 60, bitcoin = 40),
      requirements = RoleActionRequirements(minCpu = 25, minCode = 15),
      description = "Analysiere Daten für wertvolle Insights"
    ),

    // ─────── BOTNET NODE ACTIONS ───────
    RoleActionBlueprint(
      id = "launch_ddos",
      name = "Launch DDoS",
      roleType = ServerRoleType.BotnetNode,
      durationRounds = 1,
      detectionRiskIncrease = 40,
      rewards = RoleActionReward(bitcoin = 100),
      requirements = RoleActionRequirements(minCpu = 30, minRam = 25),
      description = "Starte koordinierten DDoS-Angriff"
    ),
    RoleActionBlueprint(
      id = "spread_malware",
      name = "Spread Malware",
      roleType = ServerRoleType.BotnetNode,
      durationRounds = 3,
      detectionRiskIncrease = 30,
      rewards = RoleActionReward(bitcoin = 80),
      requirements = RoleActionRequirements(minCode = 40),
      description = "Verbreite Malware über das Botnet"
    ),
    RoleActionBlueprint(
      id = "command_bots",
      name = "Command Bots",
      roleType = ServerRoleType.BotnetNode,
      durationRounds = 2,
      detectionRiskIncrease = 25,
      rewards = RoleActionReward(bitcoin = 120),
      requirements = RoleActionRequirements(minCpu = 35, minRam = 30),
      description = "Kontrolliere und kommandiere Zombie-PCs"
    ),
    RoleActionBlueprint(
      id = "scan_network",
      name = "Scan Network",
      roleType = ServerRoleType.BotnetNode,
      durationRounds = 2,
      detectionRiskIncrease = 20,
      rewards = RoleActionReward(
        networkRangeBonus = 2
      ),
      requirements = RoleActionRequirements(minCpu = 20, minCode = 30),
      description = "Scannt das Netzwerk und erweitert die Reichweite des Botnets"
    )

  )