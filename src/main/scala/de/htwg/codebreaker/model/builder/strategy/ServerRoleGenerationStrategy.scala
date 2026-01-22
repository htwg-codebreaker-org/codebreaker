// src/main/scala/de/htwg/codebreaker/model/game/strategy/ServerRoleGenerationStrategy.scala
package de.htwg.codebreaker.model.builder.strategy

import de.htwg.codebreaker.model.server.{ServerRoleBlueprint, RoleActionBlueprint}

trait ServerRoleGenerationStrategy:
  def generateRoles(): List[ServerRoleBlueprint]
  def generateActions(): List[RoleActionBlueprint]
