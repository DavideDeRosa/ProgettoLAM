package com.derosa.progettolam.retrofit

import com.derosa.progettolam.pojo.MyAudio
import com.derosa.progettolam.pojo.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface BackendApi {

    @Headers("Content-Type: application/json")
    @POST("/auth")
    fun auth(@Body user: User): Call<ResponseBody>

    @FormUrlEncoded
    @POST("/auth/token")
    fun authToken(@Field("username") username: String, @Field("password") password: String): Call<ResponseBody>

    @GET("/audio/my")
    fun myAudio(@Header("Authorization") token: String): Call<List<MyAudio>>
}