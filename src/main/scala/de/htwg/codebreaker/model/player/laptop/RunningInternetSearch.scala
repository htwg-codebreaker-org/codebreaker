// src/main/scala/de/htwg/codebreaker/model/player/laptop/RunningInternetSearch.scala
package de.htwg.codebreaker.model.player.laptop

case class RunningInternetSearch(
  startRound: Int,
  completionRound: Int,
  foundTools: List[LaptopTool]
)