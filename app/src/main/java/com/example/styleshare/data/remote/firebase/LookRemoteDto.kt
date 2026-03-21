package com.example.styleshare.data.remote.firebase

data class LookRemoteDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imagePath: String = "",
    val createdAt: Long = 0L,
    val createdByUid: String = "",
    val authorName: String = "",
    val tags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val favoritedBy: List<String> = emptyList()
)
