package com.example.myvideofeedapp.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.myvideofeedapp.domain.model.VideoModel
import com.example.myvideofeedapp.presentation.viewmodel.FeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        modifier = Modifier.fillMaxSize().systemBarsPadding()
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

                val video = state.videos[page]

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

    val coroutineScope =
        rememberCoroutineScope()

    var showCenterIcon by remember {
        mutableStateOf(false)
    }

    var isPlaying by remember {
        mutableStateOf(true)
    }

    var currentPosition by remember {
        mutableLongStateOf(0L)
    }

    var duration by remember {
        mutableLongStateOf(1L)
    }

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

                playWhenReady = true

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

            exoPlayer.play()

            isPlaying = true

        } else {

            exoPlayer.pause()

            isPlaying = false
        }
    }

    LaunchedEffect(exoPlayer) {

        while (true) {

            currentPosition =
                exoPlayer.currentPosition

            duration =
                if (exoPlayer.duration > 0)
                    exoPlayer.duration
                else
                    1L

            delay(300)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {

                detectTapGestures(

                    onTap = {

                        if (exoPlayer.isPlaying) {

                            exoPlayer.pause()

                            isPlaying = false

                        } else {

                            exoPlayer.play()

                            isPlaying = true
                        }

                        showCenterIcon = true

                        coroutineScope.launch {

                            delay(700)

                            showCenterIcon = false
                        }
                    }
                )
            }
    ) {

        AndroidView(

            factory = {

                PlayerView(context).apply {

                    player = exoPlayer

                    useController = false

                    resizeMode =
                        AspectRatioFrameLayout
                            .RESIZE_MODE_ZOOM

                    setShowBuffering(
                        PlayerView.SHOW_BUFFERING_ALWAYS
                    )
                }
            },

            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = showCenterIcon,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(
                Alignment.Center
            )
        ) {

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Black.copy(alpha = 0.5f)
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                if (isPlaying) {

                    PlayIcon()

                } else {

                    PauseIcon()
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 16.dp,
                    bottom = 40.dp,
                    end = 90.dp
                )
        ) {

            /*Text(
                text = "@android_dev",

                color = Color.White,

                fontSize = 18.sp,

                fontWeight = FontWeight.Bold
            )*/

            Text(
                text = "Vid: ${video.id}",

                color = Color.White,

                fontSize = 15.sp,

                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {

            LinearProgressIndicator(

                progress = {
                    currentPosition.toFloat() /
                            duration.toFloat()
                },

                color = Color.Red,

                trackColor = Color.Gray.copy(alpha = 0.4f),

                modifier = Modifier
                    .fillMaxWidth()
            )

            Text(

                text =
                    "${((currentPosition * 100) / duration)}%",

                color = Color.White,

                fontSize = 12.sp,

                modifier = Modifier
                    .align(Alignment.End)
                    .padding(
                        end = 8.dp,
                        top = 2.dp,
                        bottom = 4.dp
                    )
            )
        }
    }
}

@Composable
fun PlayIcon() {

    Canvas(
        modifier = Modifier.size(40.dp)
    ) {

        val path = Path().apply {

            moveTo(size.width * 0.2f, size.height * 0.1f)

            lineTo(size.width * 0.8f, size.height * 0.5f)

            lineTo(size.width * 0.2f, size.height * 0.9f)

            close()
        }

        drawPath(
            path = path,
            color = Color.White,
            style = Fill
        )
    }
}

@Composable
fun PauseIcon() {

    Canvas(
        modifier = Modifier.size(40.dp)
    ) {

        drawRect(
            color = Color.White,

            topLeft = Offset(
                size.width * 0.2f,
                size.height * 0.1f
            ),

            size = Size(
                size.width * 0.2f,
                size.height * 0.8f
            )
        )

        drawRect(
            color = Color.White,

            topLeft = Offset(
                size.width * 0.6f,
                size.height * 0.1f
            ),

            size = Size(
                size.width * 0.2f,
                size.height * 0.8f
            )
        )
    }
}