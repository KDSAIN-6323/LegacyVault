package com.legacyvault.app.di

import android.content.Context
import androidx.room.Room
import com.legacyvault.app.data.local.LegacyVaultDatabase
import com.legacyvault.app.data.local.dao.CategoryDao
import com.legacyvault.app.data.local.dao.PageDao
import com.legacyvault.app.data.repository.CategoryRepositoryImpl
import com.legacyvault.app.data.repository.PageRepositoryImpl
import com.legacyvault.app.domain.repository.CategoryRepository
import com.legacyvault.app.domain.repository.PageRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LegacyVaultDatabase =
        Room.databaseBuilder(
            context,
            LegacyVaultDatabase::class.java,
            LegacyVaultDatabase.DATABASE_NAME
        )
            // v1 was never shipped to production — allow destructive migration from it only.
            // All future migrations (2→3, 3→4, …) must be added to DatabaseMigrations.kt.
            .fallbackToDestructiveMigrationFrom(1)
            .build()

    @Provides @Singleton
    fun provideCategoryDao(db: LegacyVaultDatabase): CategoryDao = db.categoryDao()

    @Provides @Singleton
    fun providePageDao(db: LegacyVaultDatabase): PageDao = db.pageDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds @Singleton
    abstract fun bindPageRepository(impl: PageRepositoryImpl): PageRepository
}
