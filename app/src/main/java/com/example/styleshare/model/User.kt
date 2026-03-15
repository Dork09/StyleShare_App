package com.example.styleshare.model

data class UserProfile(
    val uid: String,
    val fullName: String,
    val bio: String,
    val imagePath: String?
)
