package com.alecarnevale.diplomatico.demo.cocktail

import kotlinx.serialization.Serializable

@Serializable
internal data class BaseSpirit(
  val name: String,
  val distilled: Distilled,
)
