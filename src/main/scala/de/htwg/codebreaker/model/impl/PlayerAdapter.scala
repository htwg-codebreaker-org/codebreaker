package de.htwg.codebreaker.model.impl

import de.htwg.codebreaker.model._
import de.htwg.codebreaker.model.api.IPlayer
import de.htwg.codebreaker.model.api.ITile


class PlayerAdapter(player: Player) extends IPlayer:
  override def name: String = player.name
  override def cpu: Int = player.cpu
  override def ram: Int = player.ram
  override def code: Int = player.code
  override def level: Int = player.level
  override def xp: Int = player.xp
  override def cybersecurity: Int = player.cybersecurity
  override def tile: ITile = TileAdapter(player.tile) // Optional, wenn du Tile kapselst
