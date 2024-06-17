package com.derosa.progettolam.pojo

data class UserAlreadyExists(
    val detail: String
)

data class UserCorrectlySignedUp(
    val username: String,
    val id: Int
)

data class IncorrectCredentials(
    val detail: String
)

data class UserCorrectlySignedUpToken(
    val client_id: Int,
    val client_secret: String
)

data class UserCorrectlyRemoved(
    val detail: String
)

