package com.pentechnologies.wareader2.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Steps(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "steps") val steps: String?,
    @ColumnInfo(name = "meters") val meters: String?,
    @ColumnInfo(name = "time") val time: String?,
    @ColumnInfo(name = "date") val date: String?
)