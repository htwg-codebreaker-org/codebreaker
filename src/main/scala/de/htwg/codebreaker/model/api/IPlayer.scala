// src/main/scala/de/htwg/codebreaker/model/api/IPlayer.scala
package de.htwg.codebreaker.model.api

import de.htwg.codebreaker.model.Continent
import de.htwg.codebreaker.model.api.ITile



trait IPlayer:
  def name: String
  def cpu: Int
  def ram: Int
  def code: Int
  def level: Int
  def xp: Int
  def cybersecurity: Int
  def tile: ITile
