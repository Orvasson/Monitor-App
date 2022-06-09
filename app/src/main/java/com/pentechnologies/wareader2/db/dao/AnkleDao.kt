package com.pentechnologies.wareader2.db.dao

import androidx.room.*
import com.pentechnologies.wareader2.db.Ankle
import com.pentechnologies.wareader2.db.HeartRate
import com.pentechnologies.wareader2.db.Temperature

@Dao
interface AnkleDao {
    @Query("SELECT * FROM Ankle")
    suspend fun getAll(): List<Ankle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: Ankle)

    @Insert
    suspend fun insertAll(users: List<Ankle>)

    @Delete
    suspend fun delete(user: Ankle)

    @Query("DELETE FROM Ankle WHERE date = :tdate")
    suspend fun deleteAll(tdate:String)

    @Query("DELETE FROM Ankle")
    suspend fun deleteAll()
}