package com.legacyvault.app.di

import android.content.Context
import androidx.room.Room
import com.legacyvault.app.data.local.LegacyVaultDatabase
import com.legacyvault.app.data.local.dao.AttachmentDao
import com.legacyvault.app.data.local.dao.CategoryDao
import com.legacyvault.app.data.local.dao.PageDao
import com.legacyvault.app.data.repository.AttachmentRepositoryImpl
import com.legacyvault.app.data.repository.CategoryRepositoryImpl
import com.legacyvault.app.data.repository.PageRepositoryImpl
import com.legacyvault.app.domain.repository.AttachmentRepository
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
            .fallbackToDestructiveMigration()   // dev only — swap for real migrations before release
            .build()

    @Provides @Singleton
    fun provideCategoryDao(db: LegacyVaultDatabase): CategoryDao = db.categoryDao()

    @Provides @Singleton
    fun providePageDao(db: LegacyVaultDatabase): PageDao = db.pageDao()

    @Provides @Singleton
    fun provideAttachmentDao(db: LegacyVaultDatabase): AttachmentDao = db.attachmentDao()
}

/**
 * Separate abstract module for @Binds — Hilt requires @Binds and @Provides
 * to be in different modules (or one must be abstract with companion object).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds @Singleton
    abstract fun bindPageRepository(impl: PageRepositoryImpl): PageRepository

    @Binds @Singleton
    abstract fun bindAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository
}
