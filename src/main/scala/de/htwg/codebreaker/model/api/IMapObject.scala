package de.htwg.codebreaker.model.api

import de.htwg.codebreaker.model.Continent
import de.htwg.codebreaker.model.ServerType

sealed trait IMapObject

object IMapObject:

  case class PlayerOnTile(index: Int) extends IMapObject

  case class ServerOnTile(
    index: Int,
    serverType: ServerType,
    continent: Continent
  ) extends IMapObject

  case class PlayerAndServerTile(
    playerIndex: Int,
    serverIndex: Int,
    serverType: ServerType,
    continent: Continent
  ) extends IMapObject

  case class EmptyTile(continent: Continent) extends IMapObject

