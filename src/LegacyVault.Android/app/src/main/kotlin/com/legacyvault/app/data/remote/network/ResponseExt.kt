package com.legacyvault.app.data.remote.network

import retrofit2.Response

/**
 * Returns the non-null response body or throws [ApiException].
 * Covers both HTTP errors (non-2xx) and missing bodies on 2xx responses.
 */
fun <T> Response<T>.bodyOrThrow(): T {
    if (!isSuccessful) throw ApiException(code(), message())
    return body() ?: throw ApiException(code(), "Empty response body")
}

/**
 * Throws [ApiException] if the response is not successful.
 * For use with `Response<Unit>` where the body is irrelevant.
 */
fun <T> Response<T>.throwIfError() {
    if (!isSuccessful) throw ApiException(code(), message())
}

/** Wraps a non-2xx HTTP response or a missing body. */
class ApiException(val code: Int, override val message: String) :
    RuntimeException("HTTP $code: $message")
