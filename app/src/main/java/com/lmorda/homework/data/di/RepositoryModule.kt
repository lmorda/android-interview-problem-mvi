package com.lmorda.homework.data.di

import com.lmorda.homework.data.DataRepositoryImpl
import com.lmorda.homework.domain.DataRepository
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
    abstract fun bindsDataRepository(
        dataRepositoryImpl: DataRepositoryImpl
    ): DataRepository
}
