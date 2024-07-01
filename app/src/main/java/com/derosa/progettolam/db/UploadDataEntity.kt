package com.derosa.progettolam.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uploaddata")
data class UploadDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String?,
    val longitude: Double?,
    val latitude: Double?
)