package de.htwg.codebreaker.persistence.JSON

import de.htwg.codebreaker.persistence.FileIOInterface
import de.htwg.codebreaker.model.game.Game
import de.htwg.codebreaker.model.game.GameModel
import de.htwg.codebreaker.model.game.{GameState, Phase, GameStatus}
import de.htwg.codebreaker.model.map.{Continent, Tile, WorldMap}
import de.htwg.codebreaker.model.server.{
  Server, ServerType, ServerRoleBlueprint, RoleActionBlueprint, 
  ServerRoleType, RoleActionReward, RoleActionRequirements,
  InstalledServerRole, RunningRoleAction
}
import de.htwg.codebreaker.model.player.Player
import de.htwg.codebreaker.model.player.laptop.{
  Laptop, LaptopHardware, LaptopInstalledTools, LaptopTool,
  LaptopAction, LaptopActionType, RunningLaptopAction, ActionRewards, RunningInternetSearch
}
import de.htwg.codebreaker.model.player.skill.{PlayerSkillTree, HackSkill, SocialSkill}

import scala.util.{Try, Success, Failure}
import play.api.libs.json._
import java.io.{File, PrintWriter}
import scala.io.Source

/**
 * JSON implementation of FileIO.
 * Saves and loads game state using JSON format with play-json.
 */
class FileIOJSON extends FileIOInterface:

  private val filePath = "game_save.json"

  // JSON Formatters for all model classes
  implicit val continentFormat: Format[Continent] = new Format[Continent] {
    def reads(json: JsValue): JsResult[Continent] = json.validate[String].map(Continent.valueOf)
    def writes(continent: Continent): JsValue = JsString(continent.toString)
  }

  implicit val serverTypeFormat: Format[ServerType] = new Format[ServerType] {
    def reads(json: JsValue): JsResult[ServerType] = json.validate[String].map(ServerType.valueOf)
    def writes(serverType: ServerType): JsValue = JsString(serverType.toString)
  }

  implicit val gameStatusFormat: Format[GameStatus] = new Format[GameStatus] {
    def reads(json: JsValue): JsResult[GameStatus] = json.validate[String].map(GameStatus.valueOf)
    def writes(status: GameStatus): JsValue = JsString(status.toString)
  }

  implicit val phaseFormat: Format[Phase] = new Format[Phase] {
    def reads(json: JsValue): JsResult[Phase] = json.validate[String].map(Phase.valueOf)
    def writes(phase: Phase): JsValue = JsString(phase.toString)
  }

  implicit val serverRoleTypeFormat: Format[ServerRoleType] = new Format[ServerRoleType] {
    def reads(json: JsValue): JsResult[ServerRoleType] = json.validate[String].map(ServerRoleType.valueOf)
    def writes(roleType: ServerRoleType): JsValue = JsString(roleType.toString)
  }

  implicit val laptopActionTypeFormat: Format[LaptopActionType] = new Format[LaptopActionType] {
    def reads(json: JsValue): JsResult[LaptopActionType] = json.validate[String].map(LaptopActionType.valueOf)
    def writes(actionType: LaptopActionType): JsValue = JsString(actionType.toString)
  }

  // Basic types
  implicit val tileFormat: Format[Tile] = Json.format[Tile]
  implicit val laptopHardwareFormat: Format[LaptopHardware] = Json.format[LaptopHardware]
  implicit val laptopInstalledToolsFormat: Format[LaptopInstalledTools] = Json.format[LaptopInstalledTools]
  
  // Laptop actions - WICHTIG: Reihenfolge beachten!
  implicit val laptopActionFormat: Format[LaptopAction] = Json.format[LaptopAction]
  implicit val actionRewardsFormat: Format[ActionRewards] = Json.format[ActionRewards]
  implicit val laptopToolFormat: Format[LaptopTool] = Json.format[LaptopTool]
  implicit val runningLaptopActionFormat: Format[RunningLaptopAction] = Json.format[RunningLaptopAction]
  implicit val runningInternetSearchFormat: Format[RunningInternetSearch] = Json.format[RunningInternetSearch]
  
  implicit val laptopFormat: Format[Laptop] = Json.format[Laptop]
  implicit val playerSkillTreeFormat: Format[PlayerSkillTree] = Json.format[PlayerSkillTree]
  
  // Server role stuff
  implicit val roleActionRewardFormat: Format[RoleActionReward] = Json.format[RoleActionReward]
  implicit val roleActionRequirementsFormat: Format[RoleActionRequirements] = Json.format[RoleActionRequirements]
  implicit val runningRoleActionFormat: Format[RunningRoleAction] = Json.format[RunningRoleAction]
  implicit val installedServerRoleFormat: Format[InstalledServerRole] = Json.format[InstalledServerRole]
  
  implicit val roleActionBlueprintFormat: Format[RoleActionBlueprint] = Json.format[RoleActionBlueprint]
  implicit val serverRoleBlueprintFormat: Format[ServerRoleBlueprint] = Json.format[ServerRoleBlueprint]
  
  // Player and Server
  implicit val playerFormat: Format[Player] = Json.format[Player]
  implicit val serverFormat: Format[Server] = Json.format[Server]
  
  // Skills
  implicit val hackSkillFormat: Format[HackSkill] = Json.format[HackSkill]
  implicit val socialSkillFormat: Format[SocialSkill] = Json.format[SocialSkill]

  implicit val worldMapFormat: Format[WorldMap] = new Format[WorldMap] {
    def reads(json: JsValue): JsResult[WorldMap] = {
      // We always use the default map since it's statically defined
      JsSuccess(WorldMap.defaultMap)
    }
    def writes(worldMap: WorldMap): JsValue = Json.obj(
      "width" -> worldMap.width,
      "height" -> worldMap.height
    )
  }

  implicit val gameModelFormat: Format[GameModel] = Json.format[GameModel]
  implicit val gameStateFormat: Format[GameState] = Json.format[GameState]
  implicit val gameFormat: Format[Game] = Json.format[Game]

  override def save(game: Game): Try[Unit] = Try {
    val json = Json.toJson(game)
    val prettyJson = Json.prettyPrint(json)
    val writer = new PrintWriter(new File(filePath))
    try {
      writer.write(prettyJson)
    } finally {
      writer.close()
    }
  }

  override def load(): Try[Game] = Try {
    val source = Source.fromFile(filePath)
    try {
      val jsonString = source.mkString
      val json = Json.parse(jsonString)
      json.validate[Game] match {
        case JsSuccess(game, _) => game
        case JsError(errors) => throw new Exception(s"Failed to parse JSON: $errors")
      }
    } finally {
      source.close()
    }
  }