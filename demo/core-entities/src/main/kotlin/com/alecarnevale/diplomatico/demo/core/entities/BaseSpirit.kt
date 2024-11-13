package com.alecarnevale.diplomatico.demo.core.entities

import kotlinx.serialization.Serializable

@Serializable
data class BaseSpirit(
  val name: String,
  val distilled: Distilled,
)
