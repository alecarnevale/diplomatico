package com.alecarnevale.diplomatico.demo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alecarnevale.diplomatico.demo.core.entities.BaseSpirit

@Entity
data class Cocktail(
  @PrimaryKey val name: String,
  val bseSpirit: BaseSpirit,
)
