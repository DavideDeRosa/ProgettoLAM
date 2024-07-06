package com.derosa.progettolam.viewmodel

import SingleLiveEvent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.derosa.progettolam.db.AllAudioDataEntity
import com.derosa.progettolam.db.AudioDataEntity
import com.derosa.progettolam.db.AudioDatabase
import com.derosa.progettolam.db.UploadDataEntity
import com.derosa.progettolam.pojo.AudioAllData
import com.derosa.progettolam.pojo.AudioMetaData
import com.derosa.progettolam.pojo.AudioNotFound
import com.derosa.progettolam.pojo.AudioSuccessfullyDeleted
import com.derosa.progettolam.pojo.AudioSuccessfullyHidden
import com.derosa.progettolam.pojo.AudioSuccessfullyShown
import com.derosa.progettolam.pojo.FileNotAudio
import com.derosa.progettolam.pojo.FileTooBig
import com.derosa.progettolam.pojo.MyAudio
import com.derosa.progettolam.pojo.UploadData
import com.derosa.progettolam.pojo.UserNotAuthorized
import com.derosa.progettolam.retrofit.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AudioViewModel(val audioDatabase: AudioDatabase) : ViewModel() {

    val gson = Gson()

    //LiveData /upload
    private var fileCorrectlyUploadedLiveData = MutableLiveData<UploadData>()
    private var fileCorrectlyUploadedErrorLiveData = MutableLiveData<String>()

    //LiveData /audio/my
    private var audioMyLiveData = SingleLiveEvent<List<MyAudio>>()
    private var audioMyErrorLiveData = MutableLiveData<String>()

    //LiveData /audio/all
    private var allAudioLiveData = MutableLiveData<List<AudioAllData>>()
    private var allAudioErrorLiveData = MutableLiveData<String>()

    //LiveData /audio/{id}
    private var audioByIdLiveData = SingleLiveEvent<AudioMetaData>()
    private var audioByIdErrorLiveData = MutableLiveData<String>()

    //LiveData /audio/my/{id}/hide
    private var audioHideLiveData = MutableLiveData<AudioSuccessfullyHidden>()
    private var audioHideErrorLiveData = MutableLiveData<String>()

    //LiveData /audio/my/{id}/show
    private var audioShowLiveData = MutableLiveData<AudioSuccessfullyShown>()
    private var audioShowErrorLiveData = MutableLiveData<String>()

    //LiveData /audio/{id}      DELETE
    private var audioDeleteLiveData = MutableLiveData<AudioSuccessfullyDeleted>()
    private var audioDeleteErrorLiveData = MutableLiveData<String>()

    fun uploadAudio(token: String, longitude: Double, latitude: Double, audio: MultipartBody.Part) {
        RetrofitInstance.api.uploadAudio(token, longitude, latitude, audio)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val uploadData =
                            gson.fromJson(
                                response.body()!!.string(),
                                UploadData::class.java
                            )

                        Log.d(
                            "UploadAudio 200",
                            "UploadAudio 200: " + uploadData.toString()
                        )

                        fileCorrectlyUploadedLiveData.value = uploadData
                    } else {
                        when (response.code()) {
                            401 -> {
                                val userNotAuthorized = gson.fromJson(
                                    response.errorBody()!!.string(),
                                    UserNotAuthorized::class.java
                                )

                                Log.d(
                                    "UploadAudio 401",
                                    "UploadAudio 401 User Not Authorized: " + userNotAuthorized.detail
                                )

                                fileCorrectlyUploadedErrorLiveData.value = userNotAuthorized.detail
                            }

                            413 -> {
                                val fileTooBig = gson.fromJson(
                                    response.errorBody()!!.string(),
                                    FileTooBig::class.java
                                )

                                Log.d(
                                    "UploadAudio 413",
                                    "UploadAudio 413 File too big: " + fileTooBig.detail
                                )

                                fileCorrectlyUploadedErrorLiveData.value = fileTooBig.detail
                            }

                            415 -> {
                                val fileNotAudio = gson.fromJson(
                                    response.errorBody()!!.string(),
                                    FileNotAudio::class.java
                                )

                                Log.d(
                                    "UploadAudio 415",
                                    "UploadAudio 415 File not audio: " + fileNotAudio.detail
                                )

                                fileCorrectlyUploadedErrorLiveData.value = fileNotAudio.detail
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("Fail Upload Audio", t.message.toString())

                    fileCorrectlyUploadedErrorLiveData.value = t.message
                }
            })
    }

    fun observeFileCorrectlyUploadedLiveData(): LiveData<UploadData> {
        return fileCorrectlyUploadedLiveData
    }

    fun observeFileCorrectlyUploadedErrorLiveData(): LiveData<String> {
        return fileCorrectlyUploadedErrorLiveData
    }

    fun myAudio(token: String) {
        RetrofitInstance.api.myAudio(token).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val listType = object : TypeToken<List<MyAudio>>() {}.type
                        val list: List<MyAudio> = gson.fromJson(it.string(), listType)

                        Log.d(
                            "MyAudio 200",
                            "MyAudio 200: " + list.toString()
                        )

                        audioMyLiveData.value = list
                    }
                } else {
                    if (response.code() == 401) {   //LOGICA SE UTENTE NON AUTORIZZATO
                        val userNotAuthorized = gson.fromJson(
                            response.errorBody()!!.string(),
                            UserNotAuthorized::class.java
                        )

                        Log.d(
                            "MyAudio 401",
                            "MyAudio 401 User Not Authorized: " + userNotAuthorized.detail
                        )

                        audioMyErrorLiveData.value = userNotAuthorized.detail
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Fail My Audio", t.message.toString())

                audioMyErrorLiveData.value = t.message
            }
        })
    }

    fun observeAudioMyLiveData(): LiveData<List<MyAudio>> {
        return audioMyLiveData
    }

    fun observeAudioMyErrorLiveData(): LiveData<String> {
        return audioMyErrorLiveData
    }

    fun allAudio(token: String) {
        RetrofitInstance.api.allAudio(token).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val listType = object : TypeToken<List<AudioAllData>>() {}.type
                        val list: List<AudioAllData> = gson.fromJson(it.string(), listType)

                        Log.d(
                            "AllAudio 200",
                            "AllAudio 200: " + list.toString()
                        )

                        allAudioLiveData.value = list
                    }
                } else {
                    if (response.code() == 401) {   //LOGICA SE UTENTE NON AUTORIZZATO
                        val userNotAuthorized = gson.fromJson(
                            response.errorBody()!!.string(),
                            UserNotAuthorized::class.java
                        )

                        Log.d(
                            "AllAudio 401",
                            "AllAudio 401 User Not Authorized: " + userNotAuthorized.detail
                        )

                        allAudioErrorLiveData.value = userNotAuthorized.detail
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Fail All Audio", t.message.toString())

                allAudioErrorLiveData.value = t.message
            }
        })
    }

    fun observeAllAudioLiveData(): LiveData<List<AudioAllData>> {
        return allAudioLiveData
    }

    fun observeAllAudioErrorLiveData(): LiveData<String> {
        return allAudioErrorLiveData
    }

    fun getAudioById(token: String, audioid: Int) {
        RetrofitInstance.api.getAudioById(token, audioid).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val audioMetaData =
                        gson.fromJson(
                            response.body()!!.string(),
                            AudioMetaData::class.java
                        )

                    Log.d(
                        "AudioById 200",
                        "AudioById 200: " + audioMetaData.toString()
                    )

                    audioByIdLiveData.value = audioMetaData
                } else {
                    if (response.code() == 401) {   //LOGICA SE UTENTE NON AUTORIZZATO
                        val userNotAuthorized = gson.fromJson(
                            response.errorBody()!!.string(),
                            UserNotAuthorized::class.java
                        )

                        Log.d(
                            "AudioById 401",
                            "AudioById 401 User Not Authorized: " + userNotAuthorized.detail
                        )

                        audioByIdErrorLiveData.value = userNotAuthorized.detail
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Fail AudioById", t.message.toString())

                audioByIdErrorLiveData.value = t.message
            }
        })
    }

    fun observeAudioByIdLiveData(): LiveData<AudioMetaData> {
        return audioByIdLiveData
    }

    fun observeAudioByIdErrorLiveData(): LiveData<String> {
        return audioByIdErrorLiveData
    }

    fun hideAudio(token: String, audioid: Int) {
        RetrofitInstance.api.myAudioHide(token, audioid).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val audioSuccessfullyHidden = gson.fromJson(
                        response.body()!!.string(),
                        AudioSuccessfullyHidden::class.java
                    )

                    Log.d(
                        "AudioHide 200",
                        "AudioHide 200: " + audioSuccessfullyHidden.toString()
                    )

                    audioHideLiveData.value = audioSuccessfullyHidden
                } else {
                    when (response.code()) {
                        401 -> {
                            val userNotAuthorized = gson.fromJson(
                                response.errorBody()!!.string(),
                                UserNotAuthorized::class.java
                            )

                            Log.d(
                                "AudioHide 401",
                                "AudioHide 401 User Not Authorized: " + userNotAuthorized.detail
                            )

                            audioHideErrorLiveData.value = userNotAuthorized.detail
                        }

                        404 -> {
                            val audioNotFound = gson.fromJson(
                                response.errorBody()!!.string(),
                                AudioNotFound::class.java
                            )

                            Log.d(
                                "AudioHide 404",
                                "AudioHide 404 Audio Not Found: " + audioNotFound.detail
                            )

                            audioHideErrorLiveData.value = audioNotFound.detail
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Fail AudioHide", t.message.toString())

                audioHideErrorLiveData.value = t.message
            }
        })
    }

    fun observeAudioHideLiveData(): LiveData<AudioSuccessfullyHidden> {
        return audioHideLiveData
    }

    fun observeAudioHideErrorLiveData(): LiveData<String> {
        return audioHideErrorLiveData
    }

    fun showAudio(token: String, audioid: Int) {
        RetrofitInstance.api.myAudioShow(token, audioid).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val audioSuccessfullyShown = gson.fromJson(
                        response.body()!!.string(),
                        AudioSuccessfullyShown::class.java
                    )

                    Log.d(
                        "AudioShow 200",
                        "AudioShow 200: " + audioSuccessfullyShown.toString()
                    )

                    audioShowLiveData.value = audioSuccessfullyShown
                } else {
                    when (response.code()) {
                        401 -> {
                            val userNotAuthorized = gson.fromJson(
                                response.errorBody()!!.string(),
                                UserNotAuthorized::class.java
                            )

                            Log.d(
                                "AudioShow 401",
                                "AudioShow 401 User Not Authorized: " + userNotAuthorized.detail
                            )

                            audioShowErrorLiveData.value = userNotAuthorized.detail
                        }

                        404 -> {
                            val audioNotFound = gson.fromJson(
                                response.errorBody()!!.string(),
                                AudioNotFound::class.java
                            )

                            Log.d(
                                "AudioShow 404",
                                "AudioShow 404 Audio Not Found: " + audioNotFound.detail
                            )

                            audioShowErrorLiveData.value = audioNotFound.detail
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Fail AudioShow", t.message.toString())

                audioShowErrorLiveData.value = t.message
            }
        })
    }

    fun observeAudioShowLiveData(): LiveData<AudioSuccessfullyShown> {
        return audioShowLiveData
    }

    fun observeAudioShowErrorLiveData(): LiveData<String> {
        return audioShowErrorLiveData
    }

    fun deleteAudio(token: String, audioid: Int) {
        RetrofitInstance.api.myAudioDelete(token, audioid).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val audioSuccessfullyDeleted = gson.fromJson(
                        response.body()!!.string(),
                        AudioSuccessfullyDeleted::class.java
                    )

                    Log.d(
                        "AudioDelete 200",
                        "AudioDelete 200: " + audioSuccessfullyDeleted.toString()
                    )

                    audioDeleteLiveData.value = audioSuccessfullyDeleted
                } else {
                    when (response.code()) {
                        401 -> {
                            val userNotAuthorized = gson.fromJson(
                                response.errorBody()!!.string(),
                                UserNotAuthorized::class.java
                            )

                            Log.d(
                                "AudioDelete 401",
                                "AudioDelete 401 User Not Authorized: " + userNotAuthorized.detail
                            )

                            audioDeleteErrorLiveData.value = userNotAuthorized.detail
                        }

                        404 -> {
                            val audioNotFound = gson.fromJson(
                                response.errorBody()!!.string(),
                                AudioNotFound::class.java
                            )

                            Log.d(
                                "AudioDelete 404",
                                "AudioDelete 404 Audio Not Found: " + audioNotFound.detail
                            )

                            audioDeleteErrorLiveData.value = audioNotFound.detail
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Fail AudioDelete", t.message.toString())

                audioDeleteErrorLiveData.value = t.message
            }
        })
    }

    fun observeAudioDeleteLiveData(): LiveData<AudioSuccessfullyDeleted> {
        return audioDeleteLiveData
    }

    fun observeAudioDeleteErrorLiveData(): LiveData<String> {
        return audioDeleteErrorLiveData
    }

    //SEZIONE DATABASE
    //LiveData AudioData
    private lateinit var listAudioDbLiveData: LiveData<List<AudioDataEntity>>
    private lateinit var audioDbLiveData: LiveData<AudioDataEntity>
    private lateinit var audioByCoordDbLiveData: LiveData<List<AudioDataEntity>>

    //LiveData UploadData
    private lateinit var listUploadDbLiveData: LiveData<List<UploadDataEntity>>

    //LiveData AllAudioData
    private var allAudioDbMLiveData = SingleLiveEvent<List<AllAudioDataEntity>>()
    private var allAudioDbLiveData: LiveData<List<AllAudioDataEntity>> = allAudioDbMLiveData

    fun insertAudioDb(audio: AudioDataEntity) {
        viewModelScope.launch {
            audioDatabase.audioDataDao().insertAudio(audio)
        }
    }

    fun deleteAudioDb(username: String, longitude: Double, latitude: Double) {
        viewModelScope.launch {
            audioDatabase.audioDataDao().deleteAudio(username, longitude, latitude)
        }
    }

    fun getAllAudioDb() {
        listAudioDbLiveData = audioDatabase.audioDataDao().getAllAudio()
    }

    fun observeListAudioDbLiveData(): LiveData<List<AudioDataEntity>> {
        return listAudioDbLiveData
    }

    fun getAudioById(id: Int) {
        audioDbLiveData = audioDatabase.audioDataDao().getAudioById(id)
    }

    fun observeAudioDbLiveData(): LiveData<AudioDataEntity> {
        return audioDbLiveData
    }

    fun getAudioByCoordDb(username: String, longitude: Double, latitude: Double) {
        audioByCoordDbLiveData =
            audioDatabase.audioDataDao().getAudioByCoord(username, longitude, latitude)
    }

    fun observeAudioByCoordDbLiveData(): LiveData<List<AudioDataEntity>> {
        return audioByCoordDbLiveData
    }

    fun insertUploadDb(upload: UploadDataEntity) {
        viewModelScope.launch {
            audioDatabase.uploadDataDao().insertUpload(upload)
        }
    }

    fun deleteUploadDb(username: String, longitude: Double, latitude: Double) {
        viewModelScope.launch {
            audioDatabase.uploadDataDao().deleteUpload(username, longitude, latitude)
        }
    }

    fun getAllUploadByUsernameDb(username: String) {
        listUploadDbLiveData = audioDatabase.uploadDataDao().getAllUploadByUsername(username)
    }

    fun observeListUploadDbLiveData(): LiveData<List<UploadDataEntity>> {
        return listUploadDbLiveData
    }

    fun insertAllAudioDb(audio: AllAudioDataEntity) {
        viewModelScope.launch {
            audioDatabase.allAudioDataDao().insertAllAudio(audio)
        }
    }

    fun getAllAudioByCoord(longitude: Double, latitude: Double) {
        viewModelScope.launch {
            allAudioDbMLiveData.value =
                audioDatabase.allAudioDataDao().getAllAudioByCoord(longitude, latitude)
        }
    }

    fun observeAllAudioDbLiveData(): LiveData<List<AllAudioDataEntity>> {
        return allAudioDbLiveData
    }
}