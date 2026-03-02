package com.tcontur.central.core.network

/** Wraps every API response to propagate errors cleanly through all layers. */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int? = null, val message: String) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Error   -> this
    is ApiResult.Loading -> this
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(action: (code: Int?, message: String) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) action(code, message)
    return this
}
