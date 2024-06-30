package com.derosa.progettolam.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audiodata")
data class AudioDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String?,
    val longitude: Double?,
    val latitude: Double?,
    val locationName: String?,
    val bpm: Int?,
    val danceability: Double?,
    val loudness: Double?,
    val genre: String?,
    val instrument: String?,
    val mood: String?
)
