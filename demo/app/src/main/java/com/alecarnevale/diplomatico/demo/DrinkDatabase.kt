package com.alecarnevale.diplomatico.demo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alecarnevale.diplomatico.api.HashingRoomDBVersion
import com.alecarnevale.diplomatico.demo.cocktail.Cocktail
import com.alecarnevale.diplomatico.demo.cocktail.CocktailDao
import com.alecarnevale.diplomatico.demo.cocktail.converters.BaseSpiritConverter
import com.alecarnevale.diplomatico.demo.cocktail.converters.DistilledConverter
import com.alecarnevale.diplomatico.demo.drink.Drink
import com.alecarnevale.diplomatico.demo.drink.DrinkDao

@HashingRoomDBVersion
@Database(
  entities = [Drink::class, Cocktail::class],
  version = 1,
)
@TypeConverters(
  BaseSpiritConverter::class,
  DistilledConverter::class,
)
internal abstract class DrinkDatabase : RoomDatabase() {
  abstract fun drinkDao(): DrinkDao

  abstract fun cocktailDao(): CocktailDao
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
