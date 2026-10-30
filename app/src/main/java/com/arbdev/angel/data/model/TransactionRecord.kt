package com.arbdev.angel.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    DEPOSIT,
    WITHDRAW
}

@Serializable
data class TransactionRecord(
    val id: String,
    val goalId: String,
    val goalTitle: String,
    val amount: Double,
    val type: TransactionType,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
