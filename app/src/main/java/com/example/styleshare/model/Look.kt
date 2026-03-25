package com.example.styleshare.model

data class Look(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val authorName: String,
    val isFavorite: Boolean,
    val isLiked: Boolean,
    val createdAt: Long,
    val createdByUid: String,
    val tags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0
)
