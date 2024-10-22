package com.alecarnevale.diplomatico.demo.contributes.beverage

import com.alecarnevale.diplomatico.annotations.ContributesRoomDBVersion
import kotlinx.serialization.Serializable

@ContributesRoomDBVersion(roomDB = BeverageDatabase::class)
@Serializable
internal data class Beverage(
  val name: String,
  val isAlcoholic: Boolean,
  val brand: String,
)
