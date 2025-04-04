package model

case class Player(
    name: String,
    position: (Int, Int),
    cpu: Int,
    ram: Int,
    code: Int,
    level: Int,
    xp: Int,
    cybersecurity: Int
)
