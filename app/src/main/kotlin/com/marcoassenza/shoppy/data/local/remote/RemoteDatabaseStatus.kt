package com.marcoassenza.shoppy.data.local.remote

enum class RemoteDatabaseStatus {
    Unavailable,
    Failure,
    Available,
    Unknown,
    ChangingUser,
    UserNameIsNull,
}