package com.example.myvideofeedapp.domain.repository

import com.example.myvideofeedapp.domain.model.VideoModel

interface VideoRepository {

    suspend fun getVideos(
        page: Int
    ): Result<List<VideoModel>>
}