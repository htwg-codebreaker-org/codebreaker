package de.htwg.codebreaker.model.impl

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.api._

class ServerBlueprintAdapter(blueprint: ServerBlueprint) extends IServerBlueprint:
  override def name: String = blueprint.name
  override def preferredPosition: (Int, Int) = blueprint.preferredPosition
  override def serverType: ServerType = blueprint.serverType
  override def difficultyRange: (Int, Int) = blueprint.difficultyRange
  override def rewardCpuRange: (Int, Int) = blueprint.rewardCpuRange
  override def rewardRamRange: (Int, Int) = blueprint.rewardRamRange
