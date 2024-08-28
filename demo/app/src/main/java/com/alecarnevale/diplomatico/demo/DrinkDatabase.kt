package com.alecarnevale.diplomatico.demo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alecarnevale.diplomatico.api.AutoIncrementRoomDBVersion

@AutoIncrementRoomDBVersion
@Database(
  entities = [Drink::class],
  version = 1,
)
internal abstract class DrinkDatabase : RoomDatabase() {
  abstract fun drinkDao(): DrinkDao
}

internal object DrinkDatabaseProvider {
  private var drinkDatabase: DrinkDatabase? = null

  fun get(context: Context): DrinkDatabase {
    if (drinkDatabase == null) {
      drinkDatabase =
        Room
          .databaseBuilder(
            context = context,
            klass = DrinkDatabase::class.java,
            name = "drink-database",
          ).fallbackToDestructiveMigration()
          .allowMainThreadQueries()
          .build()
    }
    return drinkDatabase!!
  }
}
