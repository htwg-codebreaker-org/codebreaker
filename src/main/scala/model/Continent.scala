package model

enum Continent:
  case NorthAmerica, SouthAmerica, Europe, Africa, Asia, Oceania, Antarctica, Ocean

  def isLand: Boolean = this != Ocean

  def short: String = this match
    case NorthAmerica  => "NA"
    case SouthAmerica  => "SA"
    case Europe         => "EU"
    case Africa         => "AF"
    case Asia           => "AS"
    case Oceania        => "OC"
    case Antarctica     => "AN"
    case Ocean          => "XX"