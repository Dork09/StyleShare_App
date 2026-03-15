package com.example.styleshare.model

data class Comment(
    val id: String,
    val lookId: String,
    val text: String,
    val authorName: String, // Or authorUid if we want to fetch user details later
    val createdAt: Long
)
