package de.htwg.codebreaker.model.api

import de.htwg.codebreaker.model.ServerType

trait IServer:
  def name: String
  def tile: ITile
  def difficulty: Int
  def rewardCpu: Int
  def rewardRam: Int
  def hacked: Boolean
  def serverType: ServerType
  def claimedBy: Option[Int]
