package com.example.myvideofeedapp.domain.model

data class VideoModel(

    val id: Int,

    val videoUrl: String,

    val thumbnail: String,

    val duration: Int,

    val width: Int,

    val height: Int,

    val userName: String
)
