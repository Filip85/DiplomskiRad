package com.example.myapplication.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.db.entities.DestinationEntity

@Database(
    entities = [DestinationEntity::class],
    version = 1
)
abstract class DestinationDatabase: RoomDatabase() {

    abstract fun getDestinationDao(): DestinationDao

    companion object {
        @Volatile
        private var instance: DestinationDatabase? = null
        private val LOCK = Any()

        operator  fun invoke(context: Context) = instance
            ?: synchronized(LOCK) {
            instance
                ?: createDatabse(
                    context
                )
                    .also { instance = it }
        }

        private fun createDatabse(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                DestinationDatabase::class.java, "DestinationDB.db").build()
    }
}