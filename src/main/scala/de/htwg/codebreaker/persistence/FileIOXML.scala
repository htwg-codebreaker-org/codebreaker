package de.htwg.codebreaker.persistence

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.game._
import scala.util.{Try, Success, Failure}
import scala.xml._
import java.io.{File, PrintWriter}

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
        <skills>
          {game.model.skills.map(skillToXML)}
        </skills>
        <worldMap>
          <width>{game.model.worldMap.width}</width>
          <height>{game.model.worldMap.height}</height>
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

      <tile>
        <x>{player.tile.x}</x>
        <y>{player.tile.y}</y>
        <continent>{player.tile.continent.toString}</continent>
      </tile>

      <cpu>{player.cpu}</cpu>
      <ram>{player.ram}</ram>
      <code>{player.code}</code>

      <availableXp>{player.availableXp}</availableXp>
      <totalXpEarned>{player.totalXpEarned}</totalXpEarned>

      <skills>
        {player.skills.unlockedSkillIds.map(id => <skill>{id}</skill>)}
      </skills>

      <cybersecurity>{player.cybersecurity}</cybersecurity>
      <movementPoints>{player.movementPoints}</movementPoints>
      <maxMovementPoints>{player.maxMovementPoints}</maxMovementPoints>
    </player>

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
      <hackedBy>{server.hackedBy.getOrElse(-1)}</hackedBy>
    </server>
  
  private def skillToXML(skill: HackSkill): Elem =
  <skill>
    <id>{skill.id}</id>
    <name>{skill.name}</name>
    <costXp>{skill.costXp}</costXp>
    <successBonus>{skill.successBonus}</successBonus>
    <description>{skill.description}</description>
  </skill>

  private def xmlToGame(xml: Node): Game =
    val model = xmlToGameModel((xml \ "model").head)
    val state = xmlToGameState((xml \ "state").head)
    Game(model, state)

  private def xmlToGameModel(xml: Node): GameModel =
    val players = (xml \ "players" \ "player").map(xmlToPlayer).toList
    val servers = (xml \ "servers" \ "server").map(xmlToServer).toList
    val worldMap = xmlToWorldMap((xml \ "worldMap").head)
    val skills = (xml \ "skills" \ "skill").map(xmlToSkill).toList

    GameModel(players, servers, worldMap, skills)

  private def xmlToPlayer(xml: Node): Player =
    Player(
      id = (xml \ "id").text.toInt,
      name = (xml \ "name").text,
      tile = xmlToTile((xml \ "tile").head),

      cpu = (xml \ "cpu").text.toInt,
      ram = (xml \ "ram").text.toInt,
      code = (xml \ "code").text.toInt,

      availableXp = (xml \ "availableXp").text.toInt,
      totalXpEarned = (xml \ "totalXpEarned").text.toInt,

      skills = PlayerSkillTree(
        unlockedSkillIds =
          (xml \ "skills" \ "skill").map(_.text).toSet
      ),

      cybersecurity = (xml \ "cybersecurity").text.toInt,
      movementPoints = (xml \ "movementPoints").text.toInt,
      maxMovementPoints = (xml \ "maxMovementPoints").text.toInt
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
      hackedBy = if (hackedByValue == -1) None else Some(hackedByValue)
    )

  private def xmlToSkill(xml: Node): HackSkill =
    HackSkill(
      id = (xml \ "id").text,
      name = (xml \ "name").text,
      costXp = (xml \ "costXp").text.toInt,
      successBonus = (xml \ "successBonus").text.toInt,
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
