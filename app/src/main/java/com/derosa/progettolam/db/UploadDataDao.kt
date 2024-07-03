package com.derosa.progettolam.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UploadDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpload(upload: UploadDataEntity)

    @Query("DELETE FROM uploaddata WHERE username = :username AND longitude = :longitude AND latitude = :latitude")
    suspend fun deleteUpload(username: String, longitude: Double, latitude: Double)

    @Query("SELECT * FROM uploaddata WHERE username = :username")
    fun getAllUploadByUsername(username: String): LiveData<List<UploadDataEntity>>

    @Query("SELECT * FROM uploaddata WHERE username = :username")
    suspend fun getAllUploadByUsernameInt(username: String): List<UploadDataEntity>
}