package com.pentechnologies.wareader2.db.dao

import androidx.room.*
import com.pentechnologies.wareader2.db.Ankle
import com.pentechnologies.wareader2.db.Ecg
import com.pentechnologies.wareader2.db.HeartRate
import com.pentechnologies.wareader2.db.Temperature

@Dao
interface EcgDao {
    @Query("SELECT * FROM Ecg")
    suspend fun getAll(): List<Ecg>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: Ecg)

    @Insert
    suspend fun insertAll(users: List<Ecg>)

    @Delete
    suspend fun delete(user: Ecg)

    @Query("DELETE FROM Ecg WHERE date = :tdate")
    suspend fun deleteAll(tdate:String)

    @Query("DELETE FROM Ecg")
    suspend fun deleteAll()
}