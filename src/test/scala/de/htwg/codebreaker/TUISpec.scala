package de.htwg.codebreaker.ui

import de.htwg.codebreaker.model._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import java.io.{ByteArrayOutputStream, PrintStream}

class TUISpec extends AnyWordSpec with Matchers {

  val map = WorldMap.defaultMap
  val tile1 = map.tileAt(3, 2).get
  val tile2 = map.tileAt(2, 2).get

  val player = Player(0, "Tester", tile1, 100, 64, 42, 1, 10, 50)
  val server = Server("TestServer", tile2, 60, 20, 15, hacked = false, ServerType.Bank)

  "TUI.displayMap" should {
    "render a map string with player and server" in {
      val result = TUI.displayMap(map, List(player), List(server))
      result should include("[P0]")
      result should include("[0]S-" + tile2.continent.short)
    }

    "show PlayerAndServerTile when sharing tile" in {
      val tile = map.tileAt(5, 2).get
      val p = Player(0, "SameSpot", tile, 0, 0, 0, 0, 0, 0)
      val s = Server("Overlap", tile, 20, 10, 10, false, ServerType.Firm)
      val result = TUI.displayMap(map, List(p), List(s))
      result should include("[P0/S0]")
    }
  }
  
  "TUI.printServerList should display all types including default match" in {
  val map = WorldMap.defaultMap
  val tile = map.tileAt(2, 2).get
  val server = Server("GenericFirm", tile, 50, 20, 15, false, ServerType.Firm)
  val out = new ByteArrayOutputStream()
  Console.withOut(new PrintStream(out)) {
    TUI.printServerList(List(server), map)
  }
  out.toString should include("GenericFirm")
  out.toString should include("Firm")
  }

  "TUI.printServerList should cover all ServerType cases" in {
    val map = WorldMap.defaultMap
    val tile = map.tileAt(1, 1).get

    val types = List(
      ServerType.Side,
      ServerType.Bank,
      ServerType.GKS,
      ServerType.Cloud,
      ServerType.Firm,
      ServerType.Military,
      ServerType.Private
    )

    val servers = types.zipWithIndex.map { case (t, i) =>
      Server(s"TestServer$i", tile.copy(x = i, y = 1), 50, 20, 10, false, t)
    }

    val output = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(output)) {
      TUI.printServerList(servers, map)
    }

    val result = output.toString
    types.foreach(t => result should include(t.toString))
  }



  "TUI.show" should {
    "output complete game state to console" in {
      val stream = new ByteArrayOutputStream()
      Console.withOut(new PrintStream(stream)) {
        TUI.show(List(player), map, List(server))
      }
      val output = stream.toString
      output should include("Willkommen zu Codebreaker!")
      output should include("Spieler 0: Tester")
      output should include("TestServer")
      output should include("CPU: 100")
    }
  }
}
