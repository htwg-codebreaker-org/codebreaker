// src/main/scala/de/htwg/codebreaker/model/game/strategy/server/DefaultServerRoleGenerator.scala
package de.htwg.codebreaker.model.game.strategy.server

import de.htwg.codebreaker.model.game.strategy.ServerRoleGenerationStrategy
import de.htwg.codebreaker.model.server.{ServerRoleBlueprint, RoleActionBlueprint}

object DefaultServerRoleGenerator extends ServerRoleGenerationStrategy:

  override def generateRoles(): List[ServerRoleBlueprint] =
    ServerRoleGenerator.generateRoleBlueprints

  override def generateActions(): List[RoleActionBlueprint] =
    ServerRoleGenerator.generateActionBlueprints
