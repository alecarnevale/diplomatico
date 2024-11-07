package com.alecarnevale.diplomatico.demo.entities.converters

import androidx.room.TypeConverter
import com.alecarnevale.diplomatico.demo.core.entities.BaseSpirit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BaseSpiritConverter {
  @TypeConverter
  fun fromBaseSpirit(baseSpirit: BaseSpirit): String = Json.encodeToString(baseSpirit)

  @TypeConverter
  fun toBaseSpirit(string: String): BaseSpirit = Json.decodeFromString(string)
}
