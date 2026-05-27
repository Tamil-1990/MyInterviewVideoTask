package com.example.myvideofeedapp.data.remote.api

import com.example.myvideofeedapp.data.remote.dto.VideoResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PexelsApi {

    @GET("videos/popular")
    suspend fun getPopularVideos(

        @Query("page")
        page: Int,

        @Query("per_page")
        perPage: Int = 10

    ): VideoResponseDto
}