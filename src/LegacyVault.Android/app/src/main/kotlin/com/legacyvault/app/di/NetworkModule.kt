package com.legacyvault.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.legacyvault.app.data.local.preferences.UserPreferencesDataStore
import com.legacyvault.app.data.remote.api.AttachmentsApiService
import com.legacyvault.app.data.remote.api.AuthApiService
import com.legacyvault.app.data.remote.api.BackupApiService
import com.legacyvault.app.data.remote.api.CategoriesApiService
import com.legacyvault.app.data.remote.api.PagesApiService
import com.legacyvault.app.data.remote.api.RemindersApiService
import com.legacyvault.app.data.remote.api.SearchApiService
import com.legacyvault.app.data.remote.api.ShoppingListsApiService
import com.legacyvault.app.data.remote.network.AuthInterceptor
import com.legacyvault.app.data.remote.network.PersistentCookieJar
import com.legacyvault.app.data.remote.network.TokenRefreshAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // ── Logging ────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (com.legacyvault.app.BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

    // ── OkHttp clients ─────────────────────────────────────────────────────
    //
    // Two named clients to break the DI cycle:
    //
    //   "auth"  — plain client (no token interceptor, no authenticator).
    //             Used only by the auth Retrofit instance that powers
    //             TokenRefreshAuthenticator and AuthApiService on the auth graph.
    //
    //   "main"  — full client (AuthInterceptor + TokenRefreshAuthenticator +
    //             PersistentCookieJar). Used by all other API services.

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthOkHttpClient(
        cookieJar: PersistentCookieJar,
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("main")
    fun provideMainOkHttpClient(
        cookieJar: PersistentCookieJar,
        authInterceptor: AuthInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator,
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(authInterceptor)
        .authenticator(tokenRefreshAuthenticator)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // ── Retrofit instances ─────────────────────────────────────────────────

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(
        @Named("auth") client: OkHttpClient,
        prefs: UserPreferencesDataStore
    ): Retrofit = buildRetrofit(client, prefs)

    @Provides
    @Singleton
    @Named("main")
    fun provideMainRetrofit(
        @Named("main") client: OkHttpClient,
        prefs: UserPreferencesDataStore
    ): Retrofit = buildRetrofit(client, prefs)

    private fun buildRetrofit(client: OkHttpClient, prefs: UserPreferencesDataStore): Retrofit {
        // The base URL is user-configurable (self-hosted). We read it once at
        // startup; if the user changes it later the app must restart (or we
        // recreate Retrofit — handled by the Settings screen via process restart).
        val baseUrl = runBlocking { prefs.apiBaseUrl.first() }
            .trimEnd('/') + "/"

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(
                json.asConverterFactory("application/json; charset=UTF-8".toMediaType())
            )
            .build()
    }

    // ── API services ────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideAuthApiService(@Named("auth") retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides @Singleton
    fun provideCategoriesApiService(@Named("main") retrofit: Retrofit): CategoriesApiService =
        retrofit.create(CategoriesApiService::class.java)

    @Provides @Singleton
    fun providePagesApiService(@Named("main") retrofit: Retrofit): PagesApiService =
        retrofit.create(PagesApiService::class.java)

    @Provides @Singleton
    fun provideAttachmentsApiService(@Named("main") retrofit: Retrofit): AttachmentsApiService =
        retrofit.create(AttachmentsApiService::class.java)

    @Provides @Singleton
    fun provideSearchApiService(@Named("main") retrofit: Retrofit): SearchApiService =
        retrofit.create(SearchApiService::class.java)

    @Provides @Singleton
    fun provideRemindersApiService(@Named("main") retrofit: Retrofit): RemindersApiService =
        retrofit.create(RemindersApiService::class.java)

    @Provides @Singleton
    fun provideShoppingListsApiService(@Named("main") retrofit: Retrofit): ShoppingListsApiService =
        retrofit.create(ShoppingListsApiService::class.java)

    @Provides @Singleton
    fun provideBackupApiService(@Named("main") retrofit: Retrofit): BackupApiService =
        retrofit.create(BackupApiService::class.java)
}
