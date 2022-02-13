package com.marcoassenza.shoppy.helpers

class DataWithStatus<T>(
    val status: Status,
    val data: T? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(message: String? = null, data: T? = null): DataWithStatus<T> {
            return DataWithStatus(Status.SUCCESS, data, message)
        }

        fun <T> error(message: String? = null, data: T? = null): DataWithStatus<T> {
            return DataWithStatus(Status.ERROR, data, message)
        }

        fun <T> loading(message: String? = null, data: T? = null): DataWithStatus<T> {
            return DataWithStatus(Status.LOADING, data, message)
        }
    }
}

enum class Status {
    LOADING,
    SUCCESS,
    ERROR,
}