package de.htwg.codebreaker.persistence

import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.game.GameModel
import de.htwg.codebreaker.model.game.{GameState, Phase, GameStatus}
import de.htwg.codebreaker.model.map.{Continent, Tile, WorldMap}
import de.htwg.codebreaker.model.server.{Server, ServerType, ServerRoleBlueprint, RoleActionBlueprint, ServerRoleType, RoleActionReward, RoleActionRequirements}
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.player.laptop.{Laptop, LaptopHardware, LaptopInstalledTools, LaptopTool, LaptopAction, LaptopActionType, RunningLaptopAction, ActionRewards, RunningInternetSearch}
import de.htwg.codebreaker.model.player.skill.{PlayerSkillTree, HackSkill, SocialSkill}

import scala.util.{Try, Success, Failure}
import scala.xml._
import java.io.{File, PrintWriter}
import de.htwg.codebreaker.model.server.RunningRoleAction
import de.htwg.codebreaker.model.server.InstalledServerRole

/**
 * XML implementation of FileIO.
 * Saves and loads game state using XML format.
 */
class FileIOXML extends FileIOInterface:

  private val filePath = "game_save.xml"

  override def save(game: Game): Try[Unit] = Try {
    val xml = gameToXML(game)
    val writer = new PrintWriter(new File(filePath))
    try {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
      writer.write(xml.toString)
    } finally {
      writer.close()
    }
  }

  override def load(): Try[Game] = Try {
    val xmlFile = XML.loadFile(filePath)
    xmlToGame(xmlFile)
  }

  private def gameToXML(game: Game): Elem =
    <game>
      <model>
        <players>
          {game.model.players.map(playerToXML)}
        </players>
        <servers>
          {game.model.servers.map(serverToXML)}
        </servers>
        <hackSkills>
          {game.model.hackSkills.map(hackSkillToXML)}
        </hackSkills>
        <socialSkills>
          {game.model.socialSkills.map(socialSkillToXML)}
        </socialSkills>
        <roleBlueprints>
          {game.model.roleBlueprints.map(roleBlueprintToXML)}
        </roleBlueprints>
        <actionBlueprints>
          {game.model.actionBlueprints.map(actionBlueprintToXML)}
        </actionBlueprints>
        <laptopTools>
          {game.model.laptopTools.map(laptopToolToXML)}
        </laptopTools>
        <worldMap>
          <width>{game.model.map.width}</width>
          <height>{game.model.map.height}</height>
        </worldMap>
      </model>
      <state>
        <currentPlayerIndex>{game.state.currentPlayerIndex.getOrElse(-1)}</currentPlayerIndex>
        <status>{game.state.status.toString}</status>
        <phase>{game.state.phase.toString}</phase>
        <round>{game.state.round}</round>
      </state>
    </game>

  private def playerToXML(player: Player): Elem =
    <player>
      <id>{player.id}</id>
      <name>{player.name}</name>
      <movementPoints>{player.movementPoints}</movementPoints>
      <maxMovementPoints>{player.maxMovementPoints}</maxMovementPoints>
      <arrested>{player.arrested}</arrested>

      <tile>
        <x>{player.tile.x}</x>
        <y>{player.tile.y}</y>
        <continent>{player.tile.continent.toString}</continent>
      </tile>

      <laptop>
        <hardware>
          <cpu>{player.laptop.hardware.cpu}</cpu>
          <ram>{player.laptop.hardware.ram}</ram>
          <code>{player.laptop.hardware.code}</code>
          <kerne>{player.laptop.hardware.kerne}</kerne>
        </hardware>
        <tools>
          {player.laptop.tools.installedTools.map(tool => <tool>{tool.id}</tool>)}
        </tools>
        <runningActions>
          {player.laptop.runningActions.map(runningActionToXML)}
        </runningActions>
        <cybersecurity>{player.laptop.cybersecurity}</cybersecurity>
      </laptop>

      <availableXp>{player.availableXp}</availableXp>
      <totalXpEarned>{player.totalXpEarned}</totalXpEarned>

      <skills>
        <hackSkills>
          {player.skills.unlockedHackSkills.map(id => <skill>{id}</skill>)}
        </hackSkills>
        <socialSkills>
          {player.skills.unlockedSocialSkills.map(id => <skill>{id}</skill>)}
        </socialSkills>
      </skills>
    </player>

  private def runningActionToXML(action: RunningLaptopAction): Elem =
    <runningAction>
      <actionId>{action.action.id}</actionId>
      <startRound>{action.startRound}</startRound>
      <completionRound>{action.completionRound}</completionRound>
      <targetServer>{action.targetServer}</targetServer>
    </runningAction>

  private def runningInternetSearchToXML(search: RunningInternetSearch): Elem =
    <runningInternetSearch>
      <startRound>{search.startRound}</startRound>
      <completionRound>{search.completionRound}</completionRound>
      <foundTools>
        {search.foundTools.map(tool => <tool>{tool.id}</tool>)}
      </foundTools>
    </runningInternetSearch>

  private def laptopToolToXML(tool: LaptopTool): Elem =
    <laptopTool>
      <id>{tool.id}</id>
      <name>{tool.name}</name>
      <hackBonus>{tool.hackBonus}</hackBonus>
      <stealthBonus>{tool.stealthBonus}</stealthBonus>
      <speedBonus>{tool.speedBonus}</speedBonus>
      <description>{tool.description}</description>
      <availableActions>
        {tool.availableActions.map(laptopActionToXML)}
      </availableActions>
    </laptopTool>

  private def laptopActionToXML(action: LaptopAction): Elem =
    <action>
      <id>{action.id}</id>
      <name>{action.name}</name>
      <actionType>{action.actionType.toString}</actionType>
      <durationRounds>{action.durationRounds}</durationRounds>
      <coreCost>{action.coreCost}</coreCost>
      <cpuCost>{action.cpuCost}</cpuCost>
      <ramCost>{action.ramCost}</ramCost>
      <description>{action.description}</description>
      <toolId>{action.toolId}</toolId>
    </action>

  private def serverToXML(server: Server): Elem =
    <server>
      <name>{server.name}</name>
      <tile>
        <x>{server.tile.x}</x>
        <y>{server.tile.y}</y>
        <continent>{server.tile.continent.toString}</continent>
      </tile>
      <serverType>{server.serverType.toString}</serverType>
      <difficulty>{server.difficulty}</difficulty>
      <rewardCpu>{server.rewardCpu}</rewardCpu>
      <rewardRam>{server.rewardRam}</rewardRam>
      <hacked>{server.hacked}</hacked>
      <hackedBy>{server.hackedBy}</hackedBy>
      <claimedBy>{server.claimedBy}</claimedBy>
      <cybersecurityLevel>{server.cybersecurityLevel}</cybersecurityLevel>
      <blockedUntilRound>{server.blockedUntilRound}</blockedUntilRound>
      <installedRole>{server.installedRole.map(roleToXML)}</installedRole>
    </server>

  private def roleToXML(role: InstalledServerRole): Elem =
    <installedRole>
      <roleType>{role.roleType.toString}</roleType>
      <installStartRound>{role.installStartRound}</installStartRound>
      <isActive>{role.isActive}</isActive>
      <detectionRisk>{role.detectionRisk}</detectionRisk>
      <runningActions>
        {role.runningActions.map(runningActionToXML)}
      </runningActions>
      <networkRange>{role.networkRange}</networkRange>
    </installedRole>

  private def runningActionToXML(action: RunningRoleAction): Elem =
    <runningAction>
      <actionId>{action.actionId}</actionId>
      <startRound>{action.startRound}</startRound>
      <completionRound>{action.completionRound}</completionRound>
      <detectionIncrease>{action.detectionIncrease}</detectionIncrease>
      <expectedRewards>
        <bitcoin>{action.expectedRewards.bitcoin}</bitcoin>
        <code>{action.expectedRewards.code}</code>
        <cpu>{action.expectedRewards.cpu}</cpu>
        <ram>{action.expectedRewards.ram}</ram>
      </expectedRewards>
    </runningAction>

  private def hackSkillToXML(skill: HackSkill): Elem =
    <skill>
      <id>{skill.id}</id>
      <name>{skill.name}</name>
      <costXp>{skill.costXp}</costXp>
      <successBonus>{skill.successBonus}</successBonus>
      <description>{skill.description}</description>
    </skill>

  private def socialSkillToXML(skill: SocialSkill): Elem =
    <skill>
      <id>{skill.id}</id>
      <name>{skill.name}</name>
      <costXp>{skill.costXp}</costXp>
      <successBonus>{skill.successBonus}</successBonus>
      <description>{skill.description}</description>
    </skill>

  private def roleBlueprintToXML(blueprint: ServerRoleBlueprint): Elem =
    <blueprint>
      <roleType>{blueprint.roleType.toString}</roleType>
      <name>{blueprint.name}</name>
      <setupDurationRounds>{blueprint.setupDurationRounds}</setupDurationRounds>
      <baseDetectionRisk>{blueprint.baseDetectionRisk}</baseDetectionRisk>
      <availableActionIds>
        {blueprint.availableActionIds.map(id => <actionId>{id}</actionId>)}
      </availableActionIds>
      <description>{blueprint.description}</description>
    </blueprint>

  private def actionBlueprintToXML(blueprint: RoleActionBlueprint): Elem =
    <blueprint>
      <id>{blueprint.id}</id>
      <name>{blueprint.name}</name>
      <roleType>{blueprint.roleType.toString}</roleType>
      <durationRounds>{blueprint.durationRounds}</durationRounds>
      <detectionRiskIncrease>{blueprint.detectionRiskIncrease}</detectionRiskIncrease>
      <rewards>
        <bitcoin>{blueprint.rewards.bitcoin}</bitcoin>
        <code>{blueprint.rewards.code}</code>
        <cpu>{blueprint.rewards.cpu}</cpu>
        <ram>{blueprint.rewards.ram}</ram>
      </rewards>
      <requirements>
        <minCpu>{blueprint.requirements.minCpu}</minCpu>
        <minRam>{blueprint.requirements.minRam}</minRam>
        <minCode>{blueprint.requirements.minCode}</minCode>
      </requirements>
      <description>{blueprint.description}</description>
    </blueprint>

  private def xmlToGame(xml: Node): Game =
    val model = xmlToGameModel((xml \ "model").head)
    val state = xmlToGameState((xml \ "state").head)
    Game(model, state)

  private def xmlToGameModel(xml: Node): GameModel =
    val laptopTools = (xml \ "laptopTools" \ "laptopTool").map(xmlToLaptopTool).toList
    val players = (xml \ "players" \ "player").map(n => xmlToPlayer(n, laptopTools)).toList
    val servers = (xml \ "servers" \ "server").map(xmlToServer).toList
    val worldMap = xmlToWorldMap((xml \ "worldMap").head)
    val hackSkills = (xml \ "hackSkills" \ "skill").map(xmlToHackSkill).toList
    val socialSkills = (xml \ "socialSkills" \ "skill").map(xmlToSocialSkill).toList
    val roleBlueprints = (xml \ "roleBlueprints" \ "blueprint").map(xmlToRoleBlueprint).toList
    val actionBlueprints = (xml \ "actionBlueprints" \ "blueprint").map(xmlToActionBlueprint).toList

    GameModel(players, servers, worldMap, hackSkills, socialSkills, roleBlueprints, actionBlueprints, laptopTools)

  private def xmlToPlayer(xml: Node, laptopTools: List[LaptopTool]): Player =
    Player(
      id = (xml \ "id").text.toInt,
      name = (xml \ "name").text,
      tile = xmlToTile((xml \ "tile").head),
      movementPoints = (xml \ "movementPoints").text.toInt,
      maxMovementPoints = (xml \ "maxMovementPoints").text.toInt,
      arrested = (xml \ "arrested").text.toBoolean,

      laptop = Laptop(
        hardware = LaptopHardware(
          cpu = (xml \ "laptop" \ "hardware" \ "cpu").text.toInt,
          ram = (xml \ "laptop" \ "hardware" \ "ram").text.toInt,
          code = (xml \ "laptop" \ "hardware" \ "code").text.toInt,
          kerne = (xml \ "laptop" \ "hardware" \ "kerne").text.toInt,
          networkRange = (xml \ "laptop" \ "hardware" \ "networkRange").text.toInt
        ),
        cybersecurity = (xml \ "laptop" \ "cybersecurity").text.toInt,
        tools = LaptopInstalledTools(
          installedTools = (xml \ "laptop" \ "tools" \ "tool")
            .map(n => laptopTools.find(_.id == n.text).get)
            .toList
        ),
        runningInternetSearch = (xml \ "laptop" \ "runningInternetSearch").headOption.map(xmlToRunningInternetSearch),
        runningActions = (xml \ "laptop" \ "runningActions" \ "runningAction")
          .map(n => xmlToRunningLaptopAction(n, laptopTools))
          .toList
      ),

      availableXp = (xml \ "availableXp").text.toInt,
      totalXpEarned = (xml \ "totalXpEarned").text.toInt,

      skills = PlayerSkillTree(
        unlockedHackSkills = (xml \ "skills" \ "hackSkills" \ "skill").map(_.text).toSet,
        unlockedSocialSkills = (xml \ "skills" \ "socialSkills" \ "skill").map(_.text).toSet
      ),
    )

  private def xmlToRunningLaptopAction(xml: Node, laptopTools: List[LaptopTool]): RunningLaptopAction =
    val actionId = (xml \ "actionId").text
    val action = laptopTools
      .flatMap(_.availableActions)
      .find(_.id == actionId)
      .getOrElse(throw new Exception(s"Action with id $actionId not found"))
    
    RunningLaptopAction(
      action = action,
      startRound = (xml \ "startRound").text.toInt,
      completionRound = (xml \ "completionRound").text.toInt,
      targetServer = (xml \ "targetServer").text,
    )

  private def xmlToLaptopTool(xml: Node): LaptopTool =
    LaptopTool(
      id = (xml \ "id").text,
      name = (xml \ "name").text,
      hackBonus = (xml \ "hackBonus").text.toInt,
      stealthBonus = (xml \ "stealthBonus").text.toInt,
      speedBonus = (xml \ "speedBonus").text.toInt,
      description = (xml \ "description").text,
      availableActions = (xml \ "availableActions" \ "action").map(xmlToLaptopAction).toList
    )
  
  private def xmlToRunningInternetSearch(xml: Node): RunningInternetSearch =
    val foundToolIds = (xml \ "foundTools" \ "tool").map(_.text).toList
    val foundTools = foundToolIds.map(id => LaptopTool(id, "", 0, 0, 0, "", Nil)) 

    RunningInternetSearch(
      startRound = (xml \ "startRound").text.toInt,
      completionRound = (xml \ "completionRound").text.toInt,
      foundTools = foundTools
    )

  private def xmlToLaptopAction(xml: Node): LaptopAction =
    LaptopAction(
      id = (xml \ "id").text,
      name = (xml \ "name").text,
      actionType = LaptopActionType.valueOf((xml \ "actionType").text),
      durationRounds = (xml \ "durationRounds").text.toInt,
      coreCost = (xml \ "coreCost").text.toInt,
      cpuCost = (xml \ "cpuCost").text.toInt,
      ramCost = (xml \ "ramCost").text.toInt,
      description = (xml \ "description").text,
      toolId = (xml \ "toolId").text,
      Rewards = ActionRewards(
        cpuGained = (xml \ "rewards" \ "cpuGained").text.toInt,
        ramGained = (xml \ "rewards" \ "ramGained").text.toInt,
        codeGained = (xml \ "rewards" \ "codeGained").text.toInt,
        xpGained = (xml \ "rewards" \ "xpGained").text.toInt
      )
    )

  private def xmlToServer(xml: Node): Server =
    val hackedByValue = (xml \ "hackedBy").text.toInt
    Server(
      name = (xml \ "name").text,
      tile = xmlToTile((xml \ "tile").head),
      serverType = ServerType.valueOf((xml \ "serverType").text),
      difficulty = (xml \ "difficulty").text.toInt,
      rewardCpu = (xml \ "rewardCpu").text.toInt,
      rewardRam = (xml \ "rewardRam").text.toInt,
      hacked = (xml \ "hacked").text.toBoolean,
      hackedBy = (xml \ "hackedBy").text.toIntOption,
      claimedBy = (xml \ "claimedBy").text.toIntOption,
      cybersecurityLevel = (xml \ "cybersecurityLevel").text.toInt,
      blockedUntilRound = (xml \ "blockedUntilRound").text.toIntOption,
      installedRole = (xml \ "installedRole").headOption.map(xmlToInstalledServerRole)
    )
  
  private def xmlToInstalledServerRole(xml: Node): InstalledServerRole =
    InstalledServerRole(
      roleType = ServerRoleType.valueOf((xml \ "roleType").text),
      installStartRound = (xml \ "installStartRound").text.toInt,
      isActive = (xml \ "isActive").text.toBoolean,
      detectionRisk = (xml \ "detectionRisk").text.toInt,
      runningActions = (xml \ "runningActions" \ "runningAction").map(xmlToRunningRoleAction).toList,
      networkRange = (xml \ "networkRange").text.toInt,
    )

  private def xmlToRunningRoleAction(xml: Node): RunningRoleAction = 
    RunningRoleAction(
      actionId = (xml \ "actionId").text,
      startRound = (xml \ "startRound").text.toInt,
      completionRound = (xml \ "completionRound").text.toInt,
      detectionIncrease = (xml \ "detectionIncrease").text.toInt,
      expectedRewards = RoleActionReward(
        bitcoin = (xml \ "expectedRewards" \ "bitcoin").text.toInt,
        code = (xml \ "expectedRewards" \ "code").text.toInt,
        cpu = (xml \ "expectedRewards" \ "cpu").text.toInt,
        ram = (xml \ "expectedRewards" \ "ram").text.toInt
      )
    )

  private def xmlToHackSkill(xml: Node): HackSkill =
    HackSkill(
      id = (xml \ "id").text,
      name = (xml \ "name").text,
      costXp = (xml \ "costXp").text.toInt,
      successBonus = (xml \ "successBonus").text.toInt,
      description = (xml \ "description").text
    )

  private def xmlToSocialSkill(xml: Node): SocialSkill =
    SocialSkill(
      id = (xml \ "id").text,
      name = (xml \ "name").text,
      costXp = (xml \ "costXp").text.toInt,
      successBonus = (xml \ "successBonus").text.toInt,
      description = (xml \ "description").text
    )

  private def xmlToRoleBlueprint(xml: Node): ServerRoleBlueprint =
    ServerRoleBlueprint(
      roleType = ServerRoleType.valueOf((xml \ "roleType").text),
      name = (xml \ "name").text,
      setupDurationRounds = (xml \ "setupDurationRounds").text.toInt,
      baseDetectionRisk = (xml \ "baseDetectionRisk").text.toInt,
      availableActionIds = (xml \ "availableActionIds" \ "actionId").map(_.text).toList,
      description = (xml \ "description").text,
      networkRange = (xml \ "networkRange").text.toInt,
    )

  private def xmlToActionBlueprint(xml: Node): RoleActionBlueprint =
    RoleActionBlueprint(
      id = (xml \ "id").text,
      name = (xml \ "name").text,
      roleType = ServerRoleType.valueOf((xml \ "roleType").text),
      durationRounds = (xml \ "durationRounds").text.toInt,
      detectionRiskIncrease = (xml \ "detectionRiskIncrease").text.toInt,
      rewards = RoleActionReward(
        bitcoin = (xml \ "rewards" \ "bitcoin").text.toInt,
        code = (xml \ "rewards" \ "code").text.toInt,
        cpu = (xml \ "rewards" \ "cpu").text.toInt,
        ram = (xml \ "rewards" \ "ram").text.toInt
      ),
      requirements = RoleActionRequirements(
        minCpu = (xml \ "requirements" \ "minCpu").text.toIntOption.getOrElse(0),
        minRam = (xml \ "requirements" \ "minRam").text.toIntOption.getOrElse(0),
        minCode = (xml \ "requirements" \ "minCode").text.toIntOption.getOrElse(0)
      ),
      description = (xml \ "description").text
    )

  private def xmlToTile(xml: Node): Tile =
    Tile(
      x = (xml \ "x").text.toInt,
      y = (xml \ "y").text.toInt,
      continent = Continent.valueOf((xml \ "continent").text)
    )

  private def xmlToWorldMap(xml: Node): WorldMap =
    val width = (xml \ "width").text.toInt
    val height = (xml \ "height").text.toInt
    WorldMap.defaultMap  // We use the default map as it's statically defined

  private def xmlToGameState(xml: Node): GameState =
    val playerIndexValue = (xml \ "currentPlayerIndex").text.toInt
    GameState(
      currentPlayerIndex = if (playerIndexValue == -1) None else Some(playerIndexValue),
      status = GameStatus.valueOf((xml \ "status").text),
      phase = Phase.valueOf((xml \ "phase").text),
      round = (xml \ "round").text.toInt
    )