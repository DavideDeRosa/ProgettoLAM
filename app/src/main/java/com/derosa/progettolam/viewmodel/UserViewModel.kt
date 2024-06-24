package com.derosa.progettolam.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.derosa.progettolam.pojo.IncorrectCredentials
import com.derosa.progettolam.pojo.User
import com.derosa.progettolam.pojo.UserAlreadyExists
import com.derosa.progettolam.pojo.UserCorrectlyRemoved
import com.derosa.progettolam.pojo.UserCorrectlySignedUp
import com.derosa.progettolam.pojo.UserCorrectlySignedUpToken
import com.derosa.progettolam.pojo.UserNotAuthorized
import com.derosa.progettolam.retrofit.RetrofitInstance
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel : ViewModel() {

    private val gson = Gson()

    //LiveData /auth
    private var userCorrectlySignedUpLiveData = MutableLiveData<UserCorrectlySignedUp>()
    private var userCorrectlySignedUpErrorLiveData = MutableLiveData<String>()

    //LiveData /auth/token
    private var userCorrectlySignedUpTokenLiveData = MutableLiveData<UserCorrectlySignedUpToken>()
    private var userCorrectlySignedUpTokenErrorLiveData = MutableLiveData<String>()

    //LiveData /auth/unsubscribe
    private var userCorrectlyRemovedLiveData = MutableLiveData<UserCorrectlyRemoved>()
    private var userCorrectlyRemovedErrorLiveData = MutableLiveData<String>()

    fun auth(user: User) {
        RetrofitInstance.api.auth(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val userCorrectlySignedUp = gson.fromJson(
                        response.body()!!.string(),
                        UserCorrectlySignedUp::class.java
                    )

                    Log.d(
                        "Auth 200",
                        "Auth 200 User Correctly SignedUp: Username" + userCorrectlySignedUp.username + " ID: " + userCorrectlySignedUp.id
                    )

                    userCorrectlySignedUpLiveData.value = userCorrectlySignedUp
                } else {
                    if (response.code() == 400) {   //LOGICA SE L'UTENTE ESISTE GIA'
                        val userAlreadyExists = gson.fromJson(
                            response.errorBody()!!.string(),
                            UserAlreadyExists::class.java
                        )

                        Log.d("Auth 400", "Auth 400 Bad Request: " + userAlreadyExists.detail)

                        userCorrectlySignedUpErrorLiveData.value = userAlreadyExists.detail
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Fail Auth", t.message.toString())

                userCorrectlySignedUpErrorLiveData.value = t.message
            }
        })
    }

    fun observeUserCorrectlySignedUpLiveData(): LiveData<UserCorrectlySignedUp> {
        return userCorrectlySignedUpLiveData
    }

    fun observeUserCorrectlySignedUpErrorLiveData(): LiveData<String> {
        return userCorrectlySignedUpErrorLiveData
    }

    fun authToken(username: String, password: String) {
        RetrofitInstance.api.authToken(username, password)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val userCorrectlySignedUp = gson.fromJson(
                            response.body()!!.string(),
                            UserCorrectlySignedUpToken::class.java
                        )
                        Log.d(
                            "Auth Token 200",
                            "Auth Token 200 User Correctly SignedUp Token: Client_id: " + userCorrectlySignedUp.client_id + " Client_secret: " + userCorrectlySignedUp.client_secret
                        )

                        userCorrectlySignedUpTokenLiveData.value = userCorrectlySignedUp
                    } else {
                        if (response.code() == 400) {   //LOGICA SE LE CREDENZIALI SONO ERRATE
                            val incorrectCredentials = gson.fromJson(
                                response.errorBody()!!.string(),
                                IncorrectCredentials::class.java
                            )

                            Log.d(
                                "Auth Token 400",
                                "Auth Token 400 Bad Request: " + incorrectCredentials.detail
                            )

                            userCorrectlySignedUpTokenErrorLiveData.value =
                                incorrectCredentials.detail
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("Fail Auth Token", t.message.toString())

                    userCorrectlySignedUpTokenErrorLiveData.value = t.message
                }
            })
    }

    fun observeUserCorrectlySignedUpTokenLiveData(): LiveData<UserCorrectlySignedUpToken> {
        return userCorrectlySignedUpTokenLiveData
    }

    fun observeUserCorrectlySignedUpTokenErrorLiveData(): LiveData<String> {
        return userCorrectlySignedUpTokenErrorLiveData
    }

    fun authUnsubscribe(token: String) {
        RetrofitInstance.api.authUnsubscribe(token).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val userCorrectlyRemoved = gson.fromJson(
                        response.body()!!.string(),
                        UserCorrectlyRemoved::class.java
                    )

                    Log.d(
                        "Auth Unsubscribe 200",
                        "Auth Unsubscribe 200 User correctly removed: " + userCorrectlyRemoved.detail
                    )

                    userCorrectlyRemovedLiveData.value = userCorrectlyRemoved
                } else {
                    if (response.code() == 401) {   //LOGICA SE UTENTE NON AUTORIZZATO
                        val userNotAuthorized = gson.fromJson(
                            response.errorBody()!!.string(),
                            UserNotAuthorized::class.java
                        )

                        Log.d(
                            "Auth Unsubscribe 401",
                            "Auth Unsubscribe 401 User Not Authorized: " + userNotAuthorized.detail
                        )

                        userCorrectlyRemovedErrorLiveData.value = userNotAuthorized.detail
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Fail Auth Unsubscribe", t.message.toString())

                userCorrectlyRemovedErrorLiveData.value = t.message
            }
        })
    }

    fun observeUserCorrectlyRemovedLiveData(): LiveData<UserCorrectlyRemoved> {
        return userCorrectlyRemovedLiveData
    }

    fun observeUserCorrectlyRemovedErrorLiveData(): LiveData<String> {
        return userCorrectlyRemovedErrorLiveData
    }

}