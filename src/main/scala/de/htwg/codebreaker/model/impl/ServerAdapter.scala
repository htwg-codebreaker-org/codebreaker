package de.htwg.codebreaker.model.impl

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.api._

class ServerAdapter(server: Server) extends IServer:
  override def name: String = server.name
  override def tile: ITile = TileAdapter(server.tile)
  override def difficulty: Int = server.difficulty
  override def rewardCpu: Int = server.rewardCpu
  override def rewardRam: Int = server.rewardRam
  override def hacked: Boolean = server.hacked
  override def serverType: ServerType = server.serverType
  override def claimedBy: Option[Int] = server.claimedBy
