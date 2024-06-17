package com.derosa.progettolam.pojo

sealed class ApiResponse {
    data class UserAlreadyExists(val detail: String) : ApiResponse()
    data class UserCorrectlySignedUp(val username: String, val id: Int) : ApiResponse()
    data class IncorrectCredentials(val detail: String) : ApiResponse()
    data class UserCorrectlySignedUpToken(val client_id: Int, val client_secret: String) : ApiResponse()
    data class UserCorrectlyRemoved(val detail: String) : ApiResponse()
}