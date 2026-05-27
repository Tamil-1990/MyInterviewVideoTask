package com.example.myvideofeedapp.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.myvideofeedapp.domain.model.VideoModel
import com.example.myvideofeedapp.presentation.viewmodel.FeedViewModel

@Composable
fun VideoFeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {

    val state by viewModel.uiState
        .collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(
        pageCount = {
            state.videos.size
        }
    )


    LaunchedEffect(pagerState.currentPage) {

        if (
            pagerState.currentPage >=
            state.videos.size - 3
        ) {

            viewModel.loadNextPage()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {


        if (
            state.isLoading &&
            state.videos.isEmpty()
        ) {

            CircularProgressIndicator(
                modifier = Modifier.align(
                    Alignment.Center
                )
            )
        }


        else {

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                val video =
                    state.videos[page]

                VideoPlayerItem(
                    video = video,

                    isVisible =
                        pagerState.currentPage == page
                )
            }
        }
    }
}

@Composable
fun VideoPlayerItem(
    video: VideoModel,
    isVisible: Boolean
) {

    val context = LocalContext.current


    val exoPlayer = remember(video.id) {

        ExoPlayer.Builder(context)
            .build()
            .apply {

                setMediaItem(
                    MediaItem.fromUri(
                        video.videoUrl
                    )
                )

                repeatMode =
                    Player.REPEAT_MODE_ONE

                prepare()
            }
    }


    DisposableEffect(Unit) {

        onDispose {

            exoPlayer.release()
        }
    }


    LaunchedEffect(isVisible) {

        if (isVisible) {

            exoPlayer.playWhenReady = true

            exoPlayer.play()

        } else {

            exoPlayer.pause()
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AndroidView(

            factory = {

                PlayerView(context).apply {

                    useController = false

                    resizeMode =
                        AspectRatioFrameLayout
                            .RESIZE_MODE_ZOOM

                    player = exoPlayer
                }
            },

            modifier = Modifier
                .fillMaxSize()
                .clickable {

                    if (exoPlayer.isPlaying) {

                        exoPlayer.pause()

                    } else {

                        exoPlayer.play()
                    }
                }
        )
    }
}