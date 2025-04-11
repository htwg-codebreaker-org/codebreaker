"render a map with player and server symbols" in {
  val customTiles = Vector(
    Tile(0, 0, Continent.Europe),
    Tile(1, 0, Continent.Europe),
    Tile(0, 1, Continent.Europe),
    Tile(1, 1, Continent.Europe)
  )
  val smallMap = WorldMap(2, 2, customTiles)

  val player = Player("TestPlayer", (0, 0), 0, 0, 0, 1, 0, 0)
  val server = Server("TestServer", (1, 1), 0, 0, 0, false, ServerType.Side)

  val output = smallMap.display(List(player), List(server))

  val expected =
    """[P0] .   
       .    [0]S-EU""".stripMargin.replaceAll("\n", "").replaceAll(" +", " ").trim

  output.replaceAll("\n", "").replaceAll(" +", " ").trim should include (expected)
}
