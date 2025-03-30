// src/main/scala/action/AttackMethod.scala
package model

enum AttackMethod:
  case BruteForce
  case Phishing
  case SQLInjection
  case DDoS
  case Keylogger
  case Wurm
  case ManInTheMiddle
  case ZeroDay