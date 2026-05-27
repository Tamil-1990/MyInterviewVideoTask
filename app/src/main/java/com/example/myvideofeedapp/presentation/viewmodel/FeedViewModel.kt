package com.example.myvideofeedapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myvideofeedapp.domain.usecase.GetVideosUseCase
import com.example.myvideofeedapp.presentation.state.FeedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(FeedUiState())

    val uiState = _uiState.asStateFlow()

    private var currentPage = 1

    private var isPaginating = false

    init {
        loadVideos()
    }

    fun loadVideos() {

        viewModelScope.launch {

            _uiState.update {
                it.copy(isLoading = true)
            }

            getVideosUseCase(currentPage)
                .onSuccess { videos ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            videos = videos
                        )
                    }
                }
                .onFailure { exception ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
        }
    }

    fun loadNextPage() {

        if (isPaginating) return

        if (_uiState.value.endReached) return

        isPaginating = true

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    paginationLoading = true
                )
            }

            currentPage++

            getVideosUseCase(currentPage)
                .onSuccess { newVideos ->

                    if (newVideos.isEmpty()) {

                        _uiState.update {
                            it.copy(
                                endReached = true
                            )
                        }

                    } else {

                        val updatedVideos =
                            (_uiState.value.videos + newVideos)
                                .distinctBy { it.id }

                        _uiState.update {
                            it.copy(
                                videos = updatedVideos
                            )
                        }
                    }
                }
                .onFailure { exception ->

                    currentPage--

                    _uiState.update {
                        it.copy(
                            paginationError = exception.message
                        )
                    }
                }

            _uiState.update {
                it.copy(
                    paginationLoading = false
                )
            }

            isPaginating = false
        }
    }
}