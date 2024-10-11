package com.alecarnevale.diplomatico.demo.cocktail.converters

import androidx.room.TypeConverter
import com.alecarnevale.diplomatico.demo.cocktail.Distilled
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class DistilledConverter {
  @TypeConverter
  fun fromDistilled(distilled: Distilled): String = Json.encodeToString(distilled)

  @TypeConverter
  fun toDistilled(string: String): Distilled = Json.decodeFromString(string)
}
