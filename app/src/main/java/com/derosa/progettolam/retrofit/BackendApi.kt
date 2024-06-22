package com.derosa.progettolam.retrofit

import com.derosa.progettolam.pojo.User
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface BackendApi {

    @Headers("Content-Type: application/json")
    @POST("auth")
    fun auth(@Body user: User): Call<ResponseBody>

    @FormUrlEncoded
    @POST("auth/token")
    fun authToken(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<ResponseBody>

    @DELETE("auth/unsubscribe")
    fun authUnsubscribe(@Header("Authorization") token: String): Call<ResponseBody>

    @POST("upload")
    fun uploadAudio(
        @Header("Authorization") token: String,
        @Query("longitude") longitude: Double,
        @Query("latitude") latitude: Double,
        @Part audio: RequestBody
    ): Call<ResponseBody>

    @GET("audio/my")
    fun myAudio(@Header("Authorization") token: String): Call<ResponseBody>

    @GET("audio/all")
    fun allAudio(@Header("Authorization") token: String): Call<ResponseBody>

    @GET("audio/{id}")
    fun getAudioById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<ResponseBody>
}