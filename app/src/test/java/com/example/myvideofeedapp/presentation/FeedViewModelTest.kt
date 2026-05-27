package com.example.myvideofeedapp.presentation

import com.example.myvideofeedapp.domain.model.VideoModel
import com.example.myvideofeedapp.domain.usecase.GetVideosUseCase
import com.example.myvideofeedapp.presentation.viewmodel.FeedViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private lateinit var viewModel: FeedViewModel

    private lateinit var getVideosUseCase: GetVideosUseCase

    @Before
    fun setup() {

        Dispatchers.setMain(StandardTestDispatcher())

        getVideosUseCase = mockk()
    }

    @After
    fun tearDown() {

        Dispatchers.resetMain()
    }

    @Test
    fun `loadVideos success should update videos list`() = runTest {

        val videos = listOf(
            VideoModel(
                id = 1,
                videoUrl = "url_1",
                thumbnail = "thumb",
                duration = 10,
                width = 720,
                height = 1280,
                userName = "User"
            )
        )

        coEvery {
            getVideosUseCase.invoke(1)
        } returns Result.success(videos)

        viewModel = FeedViewModel(getVideosUseCase)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)

        assertEquals(1, state.videos.size)

        assertEquals(1, state.videos.first().id)
    }

    @Test
    fun `loadVideos failure should update error state`() = runTest {

        coEvery {
            getVideosUseCase.invoke(1)
        } returns Result.failure(
            Exception("API Error")
        )

        viewModel = FeedViewModel(getVideosUseCase)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)

        assertEquals(
            "API Error",
            state.error
        )
    }

    @Test
    fun `loadNextPage success should append new videos`() = runTest {

        val firstPage = listOf(
            VideoModel(
                id = 1,
                videoUrl = "url_1",
                thumbnail = "thumb",
                duration = 10,
                width = 720,
                height = 1280,
                userName = "User"
            )
        )

        val secondPage = listOf(
            VideoModel(
                id = 2,
                videoUrl = "url_2",
                thumbnail = "thumb",
                duration = 10,
                width = 720,
                height = 1280,
                userName = "User"
            )
        )

        coEvery {
            getVideosUseCase.invoke(1)
        } returns Result.success(firstPage)

        coEvery {
            getVideosUseCase.invoke(2)
        } returns Result.success(secondPage)

        viewModel = FeedViewModel(getVideosUseCase)

        advanceUntilIdle()

        viewModel.loadNextPage()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(2, state.videos.size)

        assertEquals(2, state.videos.last().id)
    }

    @Test
    fun `loadNextPage failure should update pagination error`() = runTest {

        val firstPage = listOf(
            VideoModel(
                id = 1,
                videoUrl = "url_1",
                thumbnail = "thumb",
                duration = 10,
                width = 720,
                height = 1280,
                userName = "User"
            )
        )

        coEvery {
            getVideosUseCase.invoke(1)
        } returns Result.success(firstPage)

        coEvery {
            getVideosUseCase.invoke(2)
        } returns Result.failure(
            Exception("Pagination Failed")
        )

        viewModel = FeedViewModel(getVideosUseCase)

        advanceUntilIdle()

        viewModel.loadNextPage()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(
            "Pagination Failed",
            state.paginationError
        )
    }

    @Test
    fun `loadNextPage empty response should set endReached true`() = runTest {

        val firstPage = listOf(
            VideoModel(
                id = 1,
                videoUrl = "url_1",
                thumbnail = "thumb",
                duration = 10,
                width = 720,
                height = 1280,
                userName = "User"
            )
        )

        coEvery {
            getVideosUseCase.invoke(1)
        } returns Result.success(firstPage)

        coEvery {
            getVideosUseCase.invoke(2)
        } returns Result.success(emptyList())

        viewModel = FeedViewModel(getVideosUseCase)

        advanceUntilIdle()

        viewModel.loadNextPage()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state.endReached)
    }

    @Test
    fun `duplicate videos should not be added`() = runTest {

        val firstPage = listOf(
            VideoModel(
                id = 1,
                videoUrl = "url_1",
                thumbnail = "thumb",
                duration = 10,
                width = 720,
                height = 1280,
                userName = "User"
            )
        )

        val duplicatePage = listOf(
            VideoModel(
                id = 1,
                videoUrl = "url_1",
                thumbnail = "thumb",
                duration = 10,
                width = 720,
                height = 1280,
                userName = "User"
            )
        )

        coEvery {
            getVideosUseCase.invoke(1)
        } returns Result.success(firstPage)

        coEvery {
            getVideosUseCase.invoke(2)
        } returns Result.success(duplicatePage)

        viewModel = FeedViewModel(getVideosUseCase)

        advanceUntilIdle()

        viewModel.loadNextPage()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(1, state.videos.size)
    }

    @Test
    fun `multiple pagination calls should prevent duplicate API requests`() = runTest {

        val firstPage = listOf(
            VideoModel(
                id = 1,
                videoUrl = "url_1",
                thumbnail = "thumb",
                duration = 10,
                width = 720,
                height = 1280,
                userName = "User"
            )
        )

        val secondPage = listOf(
            VideoModel(
                id = 2,
                videoUrl = "url_2",
                thumbnail = "thumb",
                duration = 10,
                width = 720,
                height = 1280,
                userName = "User"
            )
        )

        coEvery {
            getVideosUseCase.invoke(1)
        } returns Result.success(firstPage)

        coEvery {
            getVideosUseCase.invoke(2)
        } coAnswers {

            delay(1000)

            Result.success(secondPage)
        }

        viewModel = FeedViewModel(getVideosUseCase)

        advanceUntilIdle()

        viewModel.loadNextPage()

        viewModel.loadNextPage()

        advanceUntilIdle()

        coVerify(exactly = 1) {
            getVideosUseCase.invoke(2)
        }
    }
}