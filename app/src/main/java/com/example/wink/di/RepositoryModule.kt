package com.example.wink.di

import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.AuthRepositoryImpl
import com.example.wink.data.repository.FakeQuizRepositoryImpl
import com.example.wink.data.repository.QuizRepository
// import com.example.wink.data.repository.AuthRepositoryImpl // <--- Sau này dùng cái này
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
         impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        impl: FakeQuizRepositoryImpl
    ): QuizRepository

    // Sau này thêm các Repository khác vào đây (vẫn dùng abstract fun)
    // abstract fun bindRizzRepository(...): ...
}