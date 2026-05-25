package com.tusizi.sakuraword.data

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Article(
    val id: String,
    val title: String,
    val content: String,
    @SerializedName("cover_url") val coverUrl: String? = null,
    val type: String, // 'article' | 'video'
    @SerializedName("video_url") val videoUrl: String? = null,
    val status: String,
    @SerializedName("published_at") val publishedAt: Date? = null,
    @SerializedName("created_at") val createdAt: Date,
    @SerializedName("view_count") val viewCount: Int = 0,
    @SerializedName("like_count") val likeCount: Int = 0,
    @SerializedName("dislike_count") val dislikeCount: Int = 0
)

data class ReactionResponse(
    @SerializedName("view_count") val viewCount: Int,
    @SerializedName("like_count") val likeCount: Int,
    @SerializedName("dislike_count") val dislikeCount: Int,
    @SerializedName("user_reaction") val userReaction: String? // 'like' | 'dislike' | null
)
