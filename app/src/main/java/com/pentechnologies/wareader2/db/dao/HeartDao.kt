package com.pentechnologies.wareader2.db.dao

import androidx.room.*
import com.pentechnologies.wareader2.db.HeartRate

@Dao
interface HeartDao {
    @Query("SELECT * FROM HeartRate")
    suspend fun getAll(): List<HeartRate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: HeartRate)

    @Insert
    suspend fun insertAll(users: List<HeartRate>)

    @Delete
    suspend fun delete(user: HeartRate)

    @Query("DELETE FROM HeartRate WHERE date = :tdate")
    suspend fun deleteHeart(tdate:String)

    @Query("DELETE FROM HeartRate")
    suspend fun deleteHeart()
}