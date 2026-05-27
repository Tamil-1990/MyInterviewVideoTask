package com.example.myvideofeedapp.di

import com.example.myvideofeedapp.data.repository.VideoRepositoryImpl
import com.example.myvideofeedapp.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class VideoRepositoryModule {

    @Binds
    abstract fun provideRepository(
        repositoryImpl: VideoRepositoryImpl
    ): VideoRepository
}