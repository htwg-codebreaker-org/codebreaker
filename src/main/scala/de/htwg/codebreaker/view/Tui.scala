package de.htwg.codebreaker.view
import de.htwg.codebreaker.controller.Controller
import de.htwg.codebreaker.util.Observer
import de.htwg.codebreaker.model.MapObject._

class Tui(controller: Controller) extends Observer {
  controller.add(this)

    def processInputLine(input: String): Unit = {
    input match
        case "q" => println("Spiel beendet.")
        case s if s.startsWith("n ") =>
            val name = s.stripPrefix("n ").trim
            val player = controller.createPlayer(name)
            println(s"Spieler '${player.name}' wurde erstellt mit ID ${player.id}.")
        case "m" => println("Aktuelle Map:")
            printMap()
        case _ => println("Unbekannter Befehl.")
    }
    
    def printMap(): Unit = {
        val mapData = controller.getMapData()
        for row <- mapData do
            val line = row.map {
                case EmptyTile(_) => " . "
                case PlayerOnTile(idx) => s"P$idx"
                case ServerOnTile(idx, _, _) => s"S$idx"
                case PlayerAndServerTile(p, s, _, _) => s"P${p}S${s}"

            }.mkString(" ")
            println(line)
    }



    override def update: Unit = println("tui is updated")
}
