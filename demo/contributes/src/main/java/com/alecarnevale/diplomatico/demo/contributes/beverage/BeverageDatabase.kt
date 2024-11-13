package com.alecarnevale.diplomatico.demo.contributes.beverage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
import com.alecarnevale.diplomatico.demo.core.entities.Soda

@HashingRoomDBVersion(
  contributes = [Soda::class], // this is a showcase to make a plain class as part of the hashing function without using ContributesRoomDBVersion annotation
)
@Database(
  entities = [
    BeverageEntity::class, // this is a showcase to make a plain class as part of the hashing function through ContributesRoomDBVersion annotation
  ],
  version = 1,
)
internal abstract class BeverageDatabase : RoomDatabase() {
  abstract fun beverageEntityDao(): BeverageEntityDao
}

internal object BeverageDatabaseProvider {
  private var beverageDatabase: BeverageDatabase? = null

  fun get(context: Context): BeverageDatabase {
    if (beverageDatabase == null) {
      beverageDatabase =
        Room
          .databaseBuilder(
            context = context,
            klass = BeverageDatabase::class.java,
            name = "beverage-database",
          ).fallbackToDestructiveMigration()
          .allowMainThreadQueries()
          .build()
    }
    return beverageDatabase!!
  }
}
