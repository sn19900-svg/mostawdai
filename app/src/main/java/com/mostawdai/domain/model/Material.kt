package com.mostawdai.domain.model

/**
 * يمثل نوع مادة في المستودع (مثلاً: زيت ورد، كحول 96%، عبوات زجاج 30مل)
 * كل مادة لها وحدة قياس يختارها المستخدم بنفسه، وكمية حالية، ومتوسط تكلفة مرجّح.
 */
data class Material(
    val id: Long = 0,
    val name: String,
    val unit: String, // مثال: مل، كغ، قطعة، لتر (نص حر يدخله المستخدم)
    val currentQuantity: Double = 0.0,
    val averageCost: Double = 0.0, // متوسط تكلفة الوحدة الواحدة (مرجّح)
    val minQuantityAlert: Double = 0.0, // حد أدنى للتنبيه (اختياري، 0 = بدون تنبيه)
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalValue: Double
        get() = currentQuantity * averageCost

    val isLowStock: Boolean
        get() = minQuantityAlert > 0.0 && currentQuantity <= minQuantityAlert
}
