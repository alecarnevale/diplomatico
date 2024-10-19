package com.alecarnevale.diplomatico.demo.beverage

import com.alecarnevale.diplomatico.api.ContributesRoomDBVersion
import com.alecarnevale.diplomatico.demo.DrinkDatabase
import kotlinx.serialization.Serializable

@ContributesRoomDBVersion(roomDB = DrinkDatabase::class)
@Serializable
internal data class Beverage(
  val name: String,
  val isAlcoholic: Boolean,
  val brand: String,
)
