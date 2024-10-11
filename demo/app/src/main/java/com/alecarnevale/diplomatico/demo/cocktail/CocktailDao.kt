package com.alecarnevale.diplomatico.demo.cocktail

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
internal interface CocktailDao {
  @Query("SELECT * FROM cocktail")
  fun getAll(): LiveData<List<Cocktail>>

  @Insert
  fun insertCocktail(cocktail: Cocktail)
}