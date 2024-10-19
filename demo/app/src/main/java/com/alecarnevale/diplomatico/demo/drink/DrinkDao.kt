package com.alecarnevale.diplomatico.demo.drink

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface DrinkDao {
  @Query("SELECT * FROM drink")
  fun getAll(): LiveData<List<Drink>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertDrink(drink: Drink)
}
