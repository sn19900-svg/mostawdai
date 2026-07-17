package com.mostawdai.domain.model

/**
 * سجل حركة مخزون واحد (إدخال أو تخريج) لمادة معينة.
 * unitCost: عند الإدخال هو سعر الشراء الفعلي، عند التخريج هو متوسط التكلفة وقت التخريج (محسوب تلقائياً).
 * sellingPricePerUnit: سعر البيع لكل وحدة، اختياري، يُستخدم فقط عند التخريج لحساب الربح الفعلي.
 */
data class StockTransaction(
    val id: Long = 0,
    val materialId: Long,
    val materialNameSnapshot: String, // لحفظ الاسم حتى لو حُذفت المادة لاحقاً (للتقارير التاريخية)
    val type: TransactionType,
    val quantity: Double,
    val unitCost: Double,
    val totalCost: Double = quantity * unitCost,
    val sellingPricePerUnit: Double? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis()
) {
    val totalRevenue: Double?
        get() = sellingPricePerUnit?.let { it * quantity }

    val profit: Double?
        get() = totalRevenue?.let { it - totalCost }
}
