package de.htwg.codebreaker

import de.htwg.codebreaker.model.game._

import de.htwg.codebreaker.controller.Controller
import de.htwg.codebreaker.view.tui

object Codebreaker:
  def main(args: Array[String]): Unit =
    val (gameModel, gameState) = GameFactory.createDefaultGame()
    val controller = new Controller(gameModel, gameState)
    val tui = new tui(controller)
