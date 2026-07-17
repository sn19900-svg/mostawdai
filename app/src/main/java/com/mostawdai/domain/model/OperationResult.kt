package com.mostawdai.domain.model

/**
 * نتيجة عملية (نجاح أو فشل مع رسالة عربية واضحة)، لتفادي استخدام Exceptions لتدفق التحكم العادي.
 */
sealed class OperationResult<out T> {
    data class Success<T>(val data: T) : OperationResult<T>()
    data class Failure(val message: String) : OperationResult<Nothing>()
}
