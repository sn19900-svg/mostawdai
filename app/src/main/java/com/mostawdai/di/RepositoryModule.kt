package com.mostawdai.di

import com.mostawdai.data.export.ExportRepositoryImpl
import com.mostawdai.data.repository.MaterialRepositoryImpl
import com.mostawdai.data.repository.TransactionRepositoryImpl
import com.mostawdai.domain.repository.ExportRepository
import com.mostawdai.domain.repository.MaterialRepository
import com.mostawdai.domain.repository.TransactionRepository
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
    abstract fun bindMaterialRepository(impl: MaterialRepositoryImpl): MaterialRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindExportRepository(impl: ExportRepositoryImpl): ExportRepository
}
