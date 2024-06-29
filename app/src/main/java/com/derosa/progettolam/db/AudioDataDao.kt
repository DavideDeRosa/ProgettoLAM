package com.derosa.progettolam.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AudioDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyAudio(audio: AudioDataEntity)

    @Delete
    suspend fun deleteMyAudio(audio: AudioDataEntity)

    @Query("SELECT * FROM audiodata")
    fun getAllMyAudio(): LiveData<AudioDataEntity>
}