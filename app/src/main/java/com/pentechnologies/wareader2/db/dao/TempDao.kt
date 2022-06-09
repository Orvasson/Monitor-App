package com.pentechnologies.wareader2.db.dao

import androidx.room.*
import com.pentechnologies.wareader2.db.HeartRate
import com.pentechnologies.wareader2.db.Temperature

@Dao
interface TempDao {
    @Query("SELECT * FROM Temperature")
    suspend fun getAll(): List<Temperature>

    @Delete
    suspend fun delete(user: Temperature)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: Temperature)

    @Insert
    suspend fun insertAll(temps: List<Temperature>)

    @Query("DELETE FROM Temperature WHERE date = :tdate")
    suspend fun deleteAll(tdate:String)

    @Query("DELETE FROM Temperature")
    suspend fun deleteAll()
}