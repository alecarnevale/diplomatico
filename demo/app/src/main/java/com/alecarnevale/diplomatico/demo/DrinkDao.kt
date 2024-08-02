package com.alecarnevale.diplomatico.demo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
internal interface DrinkDao {
  @Query("SELECT * FROM drink")
  fun getAll(): LiveData<List<Drink>>

  @Insert
  fun insertDrink(drink: Drink)
}
