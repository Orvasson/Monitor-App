package com.pentechnologies.wareader2.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Ecg(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "ecg") val ecg: String?,
    @ColumnInfo(name = "time") val time: String?,
    @ColumnInfo(name = "date") val date: String?
)