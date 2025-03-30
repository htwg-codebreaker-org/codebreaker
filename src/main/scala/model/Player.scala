package model

case class Player(
    name: String,
    position: (Int, Int),
    cpu: Int,
    ram: Int,
    code: Int,
    money: Int,
    xp: Int,
    cybersecurity: Int
):
  def level: Int = Player.levelForXp(xp)


  object Player:
    def levelForXp(xp: Int): Int =
        xp match
        case _ if xp < 50   => 1
        case _ if xp < 150  => 2
        case _ if xp < 300  => 3
        case _ if xp < 500  => 4
        case _ if xp < 800  => 5
        case _              => 6
