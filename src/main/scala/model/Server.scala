package model

enum ServerType:
  case Side, Firm, Cloud, Bank, Military, GKS, Private

case class Server(
    name: String,
    position: (Int, Int),
    difficulty: Int,
    rewardCpu: Int,
    rewardRam: Int,
    hacked: Boolean,
    serverType: ServerType
)
