package com.pentechnologies.wareader2.db.dao

import androidx.room.*
import com.pentechnologies.wareader2.db.Temperature
import com.pentechnologies.wareader2.db.SpO2

@Dao
interface SpO2Dao {
    @Query("SELECT * FROM SpO2")
    suspend fun getAll(): List<SpO2>

    @Delete
    suspend fun delete(user: SpO2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: SpO2)

    @Insert
    suspend fun insertAll(temps: List<SpO2>)

    @Query("DELETE FROM SpO2 WHERE date = :tdate")
    suspend fun deleteAll(tdate:String)

    @Query("DELETE FROM SpO2")
    suspend fun deleteAll()
}