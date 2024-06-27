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

data class MyAudio(
    var id: Int,
    var longitude: Double,
    var latitude: Double,
    var hidden: Boolean
)

data class FileTooBig(
    var detail: String
)

data class FileNotAudio(
    var detail: String
)

data class AudioAllData(
    var id: Int,
    var longitude: Double,
    var latitude: Double,
)

data class AudioSuccessfullyHidden(
    var detail: String
)

data class AudioSuccessfullyShown(
    var detail: String
)

data class AudioSuccessfullyDeleted(
    var detail: String
)

data class AudioNotFound(
    var detail: String
)