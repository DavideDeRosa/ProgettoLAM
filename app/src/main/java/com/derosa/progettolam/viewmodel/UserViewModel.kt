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
import com.derosa.progettolam.retrofit.RetrofitInstance
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel : ViewModel() {

    private val gson = Gson()

    private var userCorrectlySignedUpLiveData = MutableLiveData<UserCorrectlySignedUp>()
    private var userCorrectlySignedUpErrorLiveData = MutableLiveData<String>()

    private var userCorrectlySignedUpTokenLiveData = MutableLiveData<UserCorrectlySignedUpToken>()
    private var userCorrectlySignedUpTokenErrorLiveData = MutableLiveData<String>()

    private var userCorrectlyRemovedLiveData = MutableLiveData<UserCorrectlyRemoved>()
    private var userCorrectlyRemovedErrorLiveData = MutableLiveData<String>()

    fun authFake(user: User){   //FAKE PER PROVARE IL PASSAGGIO DA REGISTER A LOGIN
        userCorrectlySignedUpLiveData.value = UserCorrectlySignedUp(user.username, 1122)
    }

    fun auth(user: User) {
        RetrofitInstance.api.auth(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val userCorrectlySignedUp = gson.fromJson(
                        response.body()!!.string(),
                        UserCorrectlySignedUp::class.java
                    )

                    userCorrectlySignedUpLiveData.value = userCorrectlySignedUp

                    Log.d(
                        "Auth 200",
                        "User correctly signed up: ${userCorrectlySignedUp.username}, ID: ${userCorrectlySignedUp.id}"
                    )
                } else {
                    if (response.code() == 400) {   //LOGICA SE L'UTENTE ESISTE GIA'
                        val userAlreadyExists = gson.fromJson(
                            response.errorBody()!!.string(),
                            UserAlreadyExists::class.java
                        )

                        userCorrectlySignedUpErrorLiveData.value = userAlreadyExists.detail

                        Log.d("Auth 400", "Auth 400 Bad Request: " + userAlreadyExists.detail)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                userCorrectlySignedUpErrorLiveData.value = t.message

                Log.d("Fail Auth", t.message.toString())
            }
        })
    }

    fun observeUserCorrectlySignedUpLiveData(): LiveData<UserCorrectlySignedUp> {
        return userCorrectlySignedUpLiveData
    }

    fun observeUserCorrectlySignedUpErrorLiveData(): LiveData<String> {
        return userCorrectlySignedUpErrorLiveData
    }

    fun authTokenFake(username: String, password: String){ //FAKE PER PROVARE IL PASSAGGIO DA LOGIN A APP ACTIVITY
        userCorrectlySignedUpTokenLiveData.value = UserCorrectlySignedUpToken(1122, "token1122")
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

                        userCorrectlySignedUpTokenLiveData.value = userCorrectlySignedUp

                        Log.d(
                            "Auth Token 200",
                            "User correctly signed up: ${userCorrectlySignedUp.client_id}, ID: ${userCorrectlySignedUp.client_secret}"
                        )
                    } else {
                        if (response.code() == 400) {
                            //LOGICA SE LE CREDENZIALI SONO ERRATE
                            val incorrectCredentials = gson.fromJson(
                                response.errorBody()!!.string(),
                                IncorrectCredentials::class.java
                            )

                            userCorrectlySignedUpTokenErrorLiveData.value = incorrectCredentials.detail

                            Log.d(
                                "Auth Token 400",
                                "Auth Token 400 Bad Request: " + incorrectCredentials.detail
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    userCorrectlySignedUpTokenErrorLiveData.value = t.message

                    Log.d("Fail Auth Token", t.message.toString())
                }
            })
    }

    fun observeUserCorrectlySignedUpTokenLiveData(): LiveData<UserCorrectlySignedUpToken> {
        return userCorrectlySignedUpTokenLiveData
    }

    fun observeUserCorrectlySignedUpTokenErrorLiveData(): LiveData<String> {
        return userCorrectlySignedUpTokenErrorLiveData
    }

    fun authUnsubscribe(user: User) {
        RetrofitInstance.api.authUnsubscribe(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val userCorrectlyRemoved = gson.fromJson(
                        response.body()!!.string(),
                        UserCorrectlyRemoved::class.java
                    )

                    userCorrectlyRemovedLiveData.value = userCorrectlyRemoved

                    Log.d(
                        "Auth Unsubscribe 200",
                        "User correctly removed: " + userCorrectlyRemoved.detail
                    )
                } else {
                    if (response.code() == 400) {
                        //LOGICA SE LE CREDENZIALI SONO ERRATE
                        val incorrectCredentials = gson.fromJson(
                            response.errorBody()!!.string(),
                            IncorrectCredentials::class.java
                        )

                        userCorrectlyRemovedErrorLiveData.value = incorrectCredentials.detail

                        Log.d(
                            "Auth Unsubscribe 400",
                            "Auth Unsubscribe 400 Bad Request: " + incorrectCredentials.detail
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                userCorrectlyRemovedErrorLiveData.value = t.message

                Log.d("Fail Auth Unsubscribe", t.message.toString())
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