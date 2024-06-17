package com.derosa.progettolam.retrofit

import com.derosa.progettolam.pojo.ApiResponse
import com.derosa.progettolam.pojo.MyAudioList
import com.derosa.progettolam.pojo.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface BackendApi {

    @POST("/auth")
    fun auth(@Body user: User): Call<ApiResponse>

    @GET("/audio/my")
    fun myAudio(@Header("Authorization") authorizationHeader: String): Call<MyAudioList>
}