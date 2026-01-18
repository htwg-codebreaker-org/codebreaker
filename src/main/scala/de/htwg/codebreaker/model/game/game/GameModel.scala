package de.htwg.codebreaker.model.game.game
import de.htwg.codebreaker.model.map.{WorldMap}
import de.htwg.codebreaker.model.server.Server
import de.htwg.codebreaker.model.player.{Player}
import de.htwg.codebreaker.model.player.skill.HackSkill
import de.htwg.codebreaker.model.player.skill.SocialSkill
import de.htwg.codebreaker.model.server.{ServerRoleBlueprint, RoleActionBlueprint}
import de.htwg.codebreaker.model.player.laptop.LaptopTool

// model/game/GameModel.scala
case class GameModel(
  players: List[Player],
  servers: List[Server],
  map: WorldMap,
  hackSkills: List[HackSkill],
  socialSkills: List[SocialSkill],
  roleBlueprints: List[ServerRoleBlueprint],
  actionBlueprints: List[RoleActionBlueprint],
  laptopTools: List[LaptopTool],
)

