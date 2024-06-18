package com.derosa.progettolam.pojo

data class UserAlreadyExists(
    var detail: String
)

data class UserCorrectlySignedUp(
    var username: String,
    var id: Int
)

data class IncorrectCredentials(
    var detail: String
)

data class UserCorrectlySignedUpToken(
    var client_id: Int,
    var client_secret: String
)

data class UserCorrectlyRemoved(
    var detail: String
)

data class UserNotAuthorized(
    var detail: String
)