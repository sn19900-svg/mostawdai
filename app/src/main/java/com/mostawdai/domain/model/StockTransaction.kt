package com.mostawdai.domain.model

import java.util.UUID

data class StockTransaction(
    val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val materialId: Long,
    val materialNameSnapshot: String,
    val type: TransactionType,
    val quantity: Double,
    val unitCost: Double,
    val totalCost: Double = quantity * unitCost,
    val sellingPricePerUnit: Double? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalRevenue: Double?
        get() = sellingPricePerUnit?.let { it * quantity }

    val profit: Double?
        get() = totalRevenue?.let { it - totalCost }
}
