package com.pentechnologies.wareader2.db.dao

import androidx.room.*
import com.pentechnologies.wareader2.db.Steps
import com.pentechnologies.wareader2.db.Temperature

@Dao
interface StepDao {
    @Query("SELECT * FROM Steps")
    suspend fun getAll(): List<Steps>

    @Delete
    suspend fun delete(user: Steps)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: Steps)

    @Insert
    suspend fun insertAll(temps: List<Steps>)

    @Query("DELETE FROM Steps WHERE date = :tdate")
    suspend fun deleteAll(tdate:String)

    @Query("DELETE FROM Steps")
    suspend fun deleteAll()
}