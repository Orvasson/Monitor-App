package com.pentechnologies.wareader2.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pentechnologies.wareader2.db.*
import com.pentechnologies.wareader2.db.dao.*

@Database(entities = [Ankle::class, Ecg::class,Gps::class,HeartRate::class, SpO2::class,Steps::class,Temperature::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ankleDao(): AnkleDao
    abstract fun ecgDao(): EcgDao
    abstract fun gpsDao(): GpsDao
    abstract fun heartDao(): HeartDao
    abstract fun spO2Dao(): SpO2Dao
    abstract fun stepDao(): StepDao
    abstract fun tempDao(): TempDao


    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wareader_db"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }


}