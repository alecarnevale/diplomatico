package com.alecarnevale.diplomatico.demo.cocktail

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class Cocktail(
  @PrimaryKey val name: String,
  val bseSpirit: BaseSpirit,
)
