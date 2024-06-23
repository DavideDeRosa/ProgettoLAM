package com.derosa.progettolam.viewmodel

import SingleLiveEvent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.derosa.progettolam.pojo.AudioAllData
import com.derosa.progettolam.pojo.AudioMetaData
import com.derosa.progettolam.pojo.FileCorrectlyUploaded
import com.derosa.progettolam.pojo.FileNotAudio
import com.derosa.progettolam.pojo.FileTooBig
import com.derosa.progettolam.pojo.MyAudio
import com.derosa.progettolam.pojo.UserNotAuthorized
import com.derosa.progettolam.retrofit.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AudioViewModel : ViewModel() {

    val gson = Gson()

    //LiveData /upload
    private var fileCorrectlyUploadedLiveData = MutableLiveData<FileCorrectlyUploaded>()
    private var fileCorrectlyUploadedErrorLiveData = MutableLiveData<String>()

    //LiveData /audio/my
    private var audioMyLiveData = MutableLiveData<List<MyAudio>>()
    private var audioMyErrorLiveData = MutableLiveData<String>()

    //LiveData /audio/all
    private var allAudioLiveData = MutableLiveData<List<AudioAllData>>()
    private var allAudioErrorLiveData = MutableLiveData<String>()

    //LiveData /audio/{id}
    private var audioByIdLiveData = SingleLiveEvent<AudioMetaData>()
    private var audioByIdErrorLiveData = MutableLiveData<String>()

    fun uploadAudio(token: String, longitude: Double, latitude: Double, audio: RequestBody) {
        RetrofitInstance.api.uploadAudio(token, longitude, latitude, audio)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        //DA FARE QUANDO SAPRO LA STRUTTURA DEL JSON CHE MI ARRIVA CON I METADATI
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
                                    "UploadAudio 415 File too big: " + fileTooBig.detail
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

    fun observeFileCorrectlyUploadedLiveData(): LiveData<FileCorrectlyUploaded> {
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
                        gson.fromJson(response.body()!!.string(), AudioMetaData::class.java)

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
}