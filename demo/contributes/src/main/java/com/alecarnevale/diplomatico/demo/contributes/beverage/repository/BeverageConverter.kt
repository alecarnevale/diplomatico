package com.alecarnevale.diplomatico.demo.contributes.beverage.repository

import com.alecarnevale.diplomatico.demo.contributes.beverage.Beverage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object BeverageConverter {
  fun toString(beverage: Beverage): String =
    Json.encodeToString(beverage)

  fun fromString(string: String): Beverage =
    Json.decodeFromString(string)
}
