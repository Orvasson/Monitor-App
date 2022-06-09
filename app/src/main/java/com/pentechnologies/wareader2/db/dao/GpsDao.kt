package com.pentechnologies.wareader2.db.dao

import androidx.room.*
import com.pentechnologies.wareader2.db.Gps
import com.pentechnologies.wareader2.db.Temperature

@Dao
interface GpsDao {
    @Query("SELECT * FROM Gps")
    suspend fun getAll(): List<Gps>

    @Delete
    suspend fun delete(user: Gps)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: Gps)

    @Insert
    suspend fun insertAll(temps: List<Gps>)

    @Query("DELETE FROM Gps WHERE date = :tdate")
    suspend fun deleteAll(tdate:String)

    @Query("DELETE FROM Gps")
    suspend fun deleteAll()
}