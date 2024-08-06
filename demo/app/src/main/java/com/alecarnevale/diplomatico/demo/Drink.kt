package com.alecarnevale.diplomatico.demo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alecarnevale.diplomatico.api.AutoIncrementRoomDBVersion

@Entity
@AutoIncrementRoomDBVersion
internal data class Drink(
  @PrimaryKey val name: String,
  val x: Int = 1,
)
