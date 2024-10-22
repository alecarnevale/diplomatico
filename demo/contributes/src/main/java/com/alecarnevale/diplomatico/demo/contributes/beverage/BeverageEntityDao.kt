package com.alecarnevale.diplomatico.demo.contributes.beverage

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface BeverageEntityDao {
  @Query("SELECT * FROM beverageentity")
  fun getAll(): LiveData<List<BeverageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertBeverage(beverageEntity: BeverageEntity)
}
