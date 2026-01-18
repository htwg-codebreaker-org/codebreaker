// src/main/scala/de/htwg/codebreaker/model/player/laptop/LaptopInstalledTools.scala
package de.htwg.codebreaker.model.player.laptop

case class LaptopInstalledTools(
  installedTools: List[LaptopTool]
) {
  def toolIds: Set[String] = installedTools.map(_.id).toSet
  
  def getTool(id: String): Option[LaptopTool] = 
    installedTools.find(_.id == id)
  
  def hasTool(id: String): Boolean = 
    installedTools.exists(_.id == id)
}

object LaptopInstalledTools {
  def empty: LaptopInstalledTools =
    LaptopInstalledTools(List.empty)
}