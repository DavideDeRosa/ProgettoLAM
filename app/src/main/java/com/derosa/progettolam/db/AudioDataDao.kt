package com.derosa.progettolam.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AudioDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudio(audio: AudioDataEntity)

    @Query("DELETE FROM audiodata WHERE username = :username AND longitude = :longitude AND latitude = :latitude")
    suspend fun deleteAudio(username: String, longitude: Double, latitude: Double)

    @Query("SELECT * FROM audiodata")
    fun getAllAudio(): LiveData<List<AudioDataEntity>>

    @Query("SELECT * FROM audiodata WHERE id = :id")
    fun getAudioById(id: Int): LiveData<AudioDataEntity>

    @Query("SELECT * FROM audiodata WHERE username = :username AND longitude = :longitude AND latitude = :latitude")
    fun getAudioByCoord(username: String, longitude: Double, latitude: Double): LiveData<List<AudioDataEntity>>
}