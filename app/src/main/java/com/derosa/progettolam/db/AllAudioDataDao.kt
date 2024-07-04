package com.derosa.progettolam.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AllAudioDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAudio(audio: AllAudioDataEntity)

    @Query("SELECT * FROM allaudiodata WHERE longitude = :longitude AND latitude = :latitude")
    suspend fun getAllAudioByCoord(longitude: Double, latitude: Double): List<AllAudioDataEntity>
}