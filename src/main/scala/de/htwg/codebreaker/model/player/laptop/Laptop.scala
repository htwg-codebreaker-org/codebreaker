// src/main/scala/de/htwg/codebreaker/model/player/laptop/Laptop.scala
package de.htwg.codebreaker.model.player.laptop

case class Laptop(
  hardware: LaptopHardware,
  tools: LaptopInstalledTools,
  runningActions: List[RunningLaptopAction],
  cybersecurity: Int,
)