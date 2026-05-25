package com.tusizi.sakuraword.data

import retrofit2.http.GET
import retrofit2.http.Query

interface DictService {
    @GET("search")
    suspend fun search(@Query("q") query: String): SearchResponse

    companion object {
        const val BASE_URL = "https://redsun-dict.wasai-test.workers.dev/"
    }
}
