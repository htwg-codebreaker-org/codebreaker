// src/main/scala/de/htwg/codebreaker/model/Continent.scala
package de.htwg.codebreaker.model

/**
 * Alle Kontinente der Weltkarte (plus „Ocean“ für Wasserflächen).
 */
enum Continent:
  case NorthAmerica, SouthAmerica, Europe, Africa, Asia, Oceania, Antarctica, Ocean

  /** Prüft, ob dieser Kontinent begehbares Land (nicht Ozean) ist. */
  def isLand: Boolean = this != Ocean

  /**
   * Kurzkürzel für die Anzeige auf der Karte:
   * NorthAmerica  => "NA"
   * SouthAmerica  => "SA"
   * Europe        => "EU"
   * Africa        => "AF"
   * Asia          => "AS"
   * Oceania       => "OC"
   * Antarctica    => "AN"
   * Ocean         => "~~"
   */
  def short: String = this match
    case NorthAmerica => "NA"
    case SouthAmerica => "SA"
    case Europe       => "EU"
    case Africa       => "AF"
    case Asia         => "AS"
    case Oceania      => "OC"
    case Antarctica   => "AN"
    case Ocean        => "~~"
