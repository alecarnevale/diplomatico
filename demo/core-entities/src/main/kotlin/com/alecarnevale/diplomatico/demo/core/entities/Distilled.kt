package com.alecarnevale.diplomatico.demo.core.entities

import kotlinx.serialization.Serializable

@Serializable
data class Distilled(
  val name: String,
  val volume: Float = 0f,
)
