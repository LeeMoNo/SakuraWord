package com.tusizi.sakuraword.data

import retrofit2.http.*

interface ArticleService {
    @GET("api/articles")
    suspend fun getArticles(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): List<Article>

    @GET("api/articles/{id}")
    suspend fun getArticle(@Path("id") id: String): Article

    @POST("api/articles/{id}/view")
    suspend fun recordView(@Path("id") id: String)

    @GET("api/articles/{id}/reaction")
    suspend fun getReaction(
        @Path("id") id: String,
        @Query("device_id") deviceId: String
    ): ReactionResponse

    @POST("api/articles/{id}/react")
    suspend fun react(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): ReactionResponse

    companion object {
        const val BASE_URL = "https://test-worker.wasai-test.workers.dev"
    }
}
