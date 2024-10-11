package com.alecarnevale.diplomatico.demo.cocktail.converters

import androidx.room.TypeConverter
import com.alecarnevale.diplomatico.demo.cocktail.BaseSpirit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class BaseSpiritConverter {
  @TypeConverter
  fun fromBaseSpirit(baseSpirit: BaseSpirit): String = Json.encodeToString(baseSpirit)

  @TypeConverter
  fun toBaseSpirit(string: String): BaseSpirit = Json.decodeFromString(string)
}
