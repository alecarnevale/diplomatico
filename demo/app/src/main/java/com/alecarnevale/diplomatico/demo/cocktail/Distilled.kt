package com.alecarnevale.diplomatico.demo.cocktail

import kotlinx.serialization.Serializable

@Serializable
internal data class Distilled(
  val name: String,
  val volume: Float = 0f,
)
