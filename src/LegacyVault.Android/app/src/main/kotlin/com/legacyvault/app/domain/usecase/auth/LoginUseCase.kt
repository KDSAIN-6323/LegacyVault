package com.legacyvault.app.domain.usecase.auth

import com.legacyvault.app.data.remote.api.AuthApiService
import com.legacyvault.app.data.remote.dto.LoginRequest
import com.legacyvault.app.data.remote.mapper.toDomainUser
import com.legacyvault.app.data.remote.network.bodyOrThrow
import com.legacyvault.app.data.remote.network.TokenStore
import com.legacyvault.app.domain.model.User
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val api: AuthApiService,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(username: String, password: String): Result<User> = runCatching {
        val response = api.login(LoginRequest(username.trim(), password))
        val body     = response.bodyOrThrow()
        tokenStore.save(body.accessToken, body.toDomainUser())
        body.toDomainUser()
    }
}
