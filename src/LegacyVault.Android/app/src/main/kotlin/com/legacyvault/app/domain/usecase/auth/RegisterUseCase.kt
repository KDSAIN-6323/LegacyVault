package com.legacyvault.app.domain.usecase.auth

import com.legacyvault.app.data.remote.api.AuthApiService
import com.legacyvault.app.data.remote.dto.RegisterRequest
import com.legacyvault.app.data.remote.mapper.toDomainUser
import com.legacyvault.app.data.remote.network.bodyOrThrow
import com.legacyvault.app.data.remote.network.TokenStore
import com.legacyvault.app.domain.model.User
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val api: AuthApiService,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String
    ): Result<User> = runCatching {
        val response = api.register(RegisterRequest(username.trim(), email.trim(), password))
        val body     = response.bodyOrThrow()
        tokenStore.save(body.accessToken, body.toDomainUser())
        body.toDomainUser()
    }
}
