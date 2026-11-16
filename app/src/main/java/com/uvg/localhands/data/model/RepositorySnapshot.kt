package com.uvg.localhands.data.model

data class RepositorySnapshot<T>(
    val value: T,
    val isFromCache: Boolean,
    val hasPendingWrites: Boolean
)