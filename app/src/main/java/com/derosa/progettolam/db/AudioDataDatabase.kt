package com.derosa.progettolam.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AudioDataEntity::class], version = 1)
abstract class AudioDataDatabase: RoomDatabase() {

    abstract fun audioDataDao(): AudioDataDao

    companion object {
        @Volatile
        var INSTANCE: AudioDataDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AudioDataDatabase{
            if(INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                    context,
                    AudioDataDatabase::class.java,
                    "audiodata.db"
                ).fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE as AudioDataDatabase
        }
    }
}