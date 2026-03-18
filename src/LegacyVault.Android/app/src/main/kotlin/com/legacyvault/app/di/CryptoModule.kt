package com.legacyvault.app.di

import com.legacyvault.app.crypto.CryptoService
import com.legacyvault.app.crypto.CryptoServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CryptoModule {

    @Binds
    @Singleton
    abstract fun bindCryptoService(impl: CryptoServiceImpl): CryptoService
}
