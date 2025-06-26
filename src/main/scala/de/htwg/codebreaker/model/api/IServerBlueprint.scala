package de.htwg.codebreaker.model.api

import de.htwg.codebreaker.model.ServerType

trait IServerBlueprint:
  def name: String
  def preferredPosition: (Int, Int)
  def serverType: ServerType
  def difficultyRange: (Int, Int)
  def rewardCpuRange: (Int, Int)
  def rewardRamRange: (Int, Int)
