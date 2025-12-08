package com.example.wink.di

import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.AuthRepositoryImpl
import com.example.wink.data.repository.FakeQuizRepositoryImpl
import com.example.wink.data.repository.QuizRepository
import com.example.wink.data.repository.QuizRepositoryImpl
import com.example.wink.data.repository.UserRepository
import com.example.wink.data.repository.UserRepositoryImpl
import com.example.wink.data.repository.SocialRepository
import com.example.wink.data.repository.SocialRepositoryImpl
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
        impl: QuizRepositoryImpl
    ): QuizRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository


    // Sau này thêm các Repository khác vào đây (vẫn dùng abstract fun)
    @Binds
    @Singleton
    abstract fun bindSocialRepository(
        impl: SocialRepositoryImpl
    ): SocialRepository
}