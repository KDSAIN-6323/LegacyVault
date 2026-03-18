package com.legacyvault.app.domain.usecase.auth

import com.legacyvault.app.data.remote.api.AuthApiService
import com.legacyvault.app.data.remote.dto.ResetPasswordRequest
import com.legacyvault.app.data.remote.network.throwIfError
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val api: AuthApiService
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        api.resetPassword(ResetPasswordRequest(username.trim(), email.trim(), newPassword))
            .throwIfError()
    }
}
