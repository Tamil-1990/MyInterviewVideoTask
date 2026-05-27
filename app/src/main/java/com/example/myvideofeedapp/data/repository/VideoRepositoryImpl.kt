package com.example.myvideofeedapp.data.repository

import com.example.myvideofeedapp.data.mapper.toDomain
import com.example.myvideofeedapp.data.remote.api.PexelsApi
import com.example.myvideofeedapp.domain.model.VideoModel
import com.example.myvideofeedapp.domain.repository.VideoRepository
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val api: PexelsApi
) : VideoRepository {

    override suspend fun getVideos(
        page: Int
    ): Result<List<VideoModel>> {

        return try {

            val response = api.getPopularVideos(
                page = page
            )

            val videos = response.videos
                .mapNotNull { dto ->

                    dto.toDomain()
                }
                .distinctBy { it.id }

            Result.success(videos)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}