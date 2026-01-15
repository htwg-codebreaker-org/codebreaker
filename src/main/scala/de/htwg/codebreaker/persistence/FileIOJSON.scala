package de.htwg.codebreaker.persistence

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import play.api.libs.json._

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.{Failure, Success, Try}

/** JSON implementation of FileIO. Saves and loads game state using JSON format with play-json.
  */
class FileIOJSON extends FileIOInterface:

  private val filePath = "game_save.json"

  // JSON Formatters for all model classes
  implicit val continentFormat: Format[Continent] = new Format[Continent] {
    def reads(json: JsValue): JsResult[Continent] = json.validate[String].map(Continent.valueOf)
    def writes(continent: Continent): JsValue     = JsString(continent.toString)
  }

  implicit val serverTypeFormat: Format[ServerType] = new Format[ServerType] {
    def reads(json: JsValue): JsResult[ServerType] = json.validate[String].map(ServerType.valueOf)
    def writes(serverType: ServerType): JsValue    = JsString(serverType.toString)
  }

  implicit val gameStatusFormat: Format[GameStatus] = new Format[GameStatus] {
    def reads(json: JsValue): JsResult[GameStatus] = json.validate[String].map(GameStatus.valueOf)
    def writes(status: GameStatus): JsValue        = JsString(status.toString)
  }

  implicit val phaseFormat: Format[Phase] = new Format[Phase] {
    def reads(json: JsValue): JsResult[Phase] = json.validate[String].map(Phase.valueOf)
    def writes(phase: Phase): JsValue         = JsString(phase.toString)
  }

  implicit val tileFormat: Format[Tile]     = Json.format[Tile]
  implicit val playerFormat: Format[Player] = Json.format[Player]
  implicit val serverFormat: Format[Server] = Json.format[Server]

  implicit val worldMapFormat: Format[WorldMap] = new Format[WorldMap] {
    def reads(json: JsValue): JsResult[WorldMap] =
      // We always use the default map since it's statically defined
      JsSuccess(WorldMap.defaultMap)
    def writes(worldMap: WorldMap): JsValue = Json.obj(
      "width"  -> worldMap.width,
      "height" -> worldMap.height
    )
  }

  implicit val gameModelFormat: Format[GameModel] = Json.format[GameModel]
  implicit val gameStateFormat: Format[GameState] = Json.format[GameState]
  implicit val gameFormat: Format[Game]           = Json.format[Game]

  override def save(game: Game): Try[Unit] = Try {
    val json       = Json.toJson(game)
    val prettyJson = Json.prettyPrint(json)
    val writer     = new PrintWriter(new File(filePath))
    try
      writer.write(prettyJson)
    finally
      writer.close()
  }

  override def load(): Try[Game] = Try {
    val source = Source.fromFile(filePath)
    try {
      val jsonString = source.mkString
      val json       = Json.parse(jsonString)
      json.validate[Game] match {
        case JsSuccess(game, _) => game
        case JsError(errors)    => throw new Exception(s"Failed to parse JSON: $errors")
      }
    } finally source.close()
  }
