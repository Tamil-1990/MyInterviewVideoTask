package com.example.myvideofeedapp.presentation.state

import com.example.myvideofeedapp.domain.model.VideoModel

data class FeedUiState(

    val isLoading: Boolean = false,

    val videos: List<VideoModel> = emptyList(),

    val paginationLoading: Boolean = false,

    val error: String? = null,

    val paginationError: String? = null,

    val endReached: Boolean = false
)