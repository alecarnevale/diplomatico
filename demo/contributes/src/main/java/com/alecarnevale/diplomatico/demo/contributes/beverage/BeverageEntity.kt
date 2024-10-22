package com.alecarnevale.diplomatico.demo.contributes.beverage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class BeverageEntity(
  @PrimaryKey val name: String,
  val anything: String,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BeverageEntity

    if (name != other.name) return false
    if (!anything.contentEquals(other.anything)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + anything.hashCode()
    return result
  }
}
