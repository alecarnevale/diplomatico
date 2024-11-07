package com.alecarnevale.diplomatico.demo.entities.converters

import androidx.room.TypeConverter
import com.alecarnevale.diplomatico.demo.core.entities.Distilled
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DistilledConverter {
  @TypeConverter
  fun fromDistilled(distilled: Distilled): String = Json.encodeToString(distilled)

  @TypeConverter
  fun toDistilled(string: String): Distilled = Json.decodeFromString(string)
}
