package com.mostawdai.domain.model

import java.util.UUID

data class Material(
    val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val materialNumber: String = "",
    val name: String,
    val unit: String,
    val currentQuantity: Double = 0.0,
    val averageCost: Double = 0.0,
    val minQuantityAlert: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalValue: Double
        get() = currentQuantity * averageCost

    val isLowStock: Boolean
        get() = minQuantityAlert > 0.0 && currentQuantity <= minQuantityAlert
}
