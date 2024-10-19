package com.alecarnevale.diplomatico.demo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alecarnevale.diplomatico.api.HashingRoomDBVersion
import com.alecarnevale.diplomatico.demo.beverage.BeverageEntity
import com.alecarnevale.diplomatico.demo.beverage.BeverageEntityDao
import com.alecarnevale.diplomatico.demo.cocktail.Cocktail
import com.alecarnevale.diplomatico.demo.cocktail.CocktailDao
import com.alecarnevale.diplomatico.demo.cocktail.converters.BaseSpiritConverter
import com.alecarnevale.diplomatico.demo.cocktail.converters.DistilledConverter
import com.alecarnevale.diplomatico.demo.drink.Drink
import com.alecarnevale.diplomatico.demo.drink.DrinkDao

@HashingRoomDBVersion
@Database(
  entities = [
    Drink::class, // this is a showcase for the base case: a change in a Room Entity must trigger a change in the report
    Cocktail::class, // this is a showcase for the nested classes case: when there is a change in a nested class then the report must change
    BeverageEntity::class, // this is a showcase to make a plain class as part of the hashing function through ContributesRoomDBVersion annotation
  ],
  version = 1,
)
@TypeConverters(
  BaseSpiritConverter::class,
  DistilledConverter::class,
)
internal abstract class DrinkDatabase : RoomDatabase() {
  abstract fun drinkDao(): DrinkDao

  abstract fun cocktailDao(): CocktailDao

  abstract fun beverageEntityDao(): BeverageEntityDao
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
