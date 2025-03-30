package model

enum ServerType:
  case Side, Firm, Cloud, Bank, Military, GKS, Private

case class Server(
    name: String,
    position: (Int, Int),
    difficulty: Int,
    rewardCpu: Int,
    rewardRam: Int,
    rewardCode: Int,
    rewardMoney: Int,
    rewardXp: Int,
    hacked: Boolean,
    serverType: ServerType
)

