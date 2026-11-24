package com.creatorsclub.core.common.result

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable? = null, val message: String = "") : Result<Nothing>
    data object Loading : Result<Nothing>
}
