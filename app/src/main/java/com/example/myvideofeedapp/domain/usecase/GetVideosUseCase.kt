package com.example.myvideofeedapp.domain.usecase

import com.example.myvideofeedapp.domain.repository.VideoRepository
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(
    private val repository: VideoRepository
) {

    suspend operator fun invoke(
        page: Int
    ) = repository.getVideos(page)
}