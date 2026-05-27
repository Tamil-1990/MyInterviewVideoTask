package com.example.myvideofeedapp.data.mapper

import com.example.myvideofeedapp.data.remote.dto.Video
import com.example.myvideofeedapp.domain.model.VideoModel

fun Video.toDomain(): VideoModel? {

    val selectedVideoFile = video_files
        .filter {
            it.file_type.contains("mp4")
        }
        .sortedByDescending {

            when {

                it.width >= 1080 &&
                        it.quality.equals(
                            "hd",
                            ignoreCase = true
                        ) -> 4

                it.width >= 720 -> 3

                it.width >= 480 -> 2

                else -> 1
            }
        }
        .firstOrNull()

    return selectedVideoFile?.let {

        VideoModel(
            id = id,
            videoUrl = it.link,
            thumbnail = image,
            duration = duration,
            width = width,
            height = height,
            userName = user.name
        )
    }
}