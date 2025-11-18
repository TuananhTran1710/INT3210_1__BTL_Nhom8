package com.example.wink.di

import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.FakeAuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    // Dùng @Binds để báo Hilt biết
    // khi ai đó yêu cầu AuthRepository
    // thì hãy cung cấp một FakeAuthRepositoryImpl
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FakeAuthRepositoryImpl
    ): AuthRepository

    // Thêm các repository khác ở đây (ví dụ: RizzRepository, ChatRepository...)
}