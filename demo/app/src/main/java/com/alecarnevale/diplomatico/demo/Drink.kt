package com.alecarnevale.diplomatico.demo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class Drink(
  @PrimaryKey val name: String,
)
