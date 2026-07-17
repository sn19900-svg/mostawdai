package com.mostawdai.domain.usecase

import com.mostawdai.domain.model.Material
import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.model.StockTransaction
import com.mostawdai.domain.model.TransactionType
import com.mostawdai.domain.repository.MaterialRepository
import com.mostawdai.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * تخريج (استخدام/بيع) كمية من مادة. التكلفة المحسوبة تلقائياً = الكمية × متوسط التكلفة الحالي.
 * متوسط التكلفة لا يتغيّر عند التخريج، فقط الكمية تنقص.
 * sellingPricePerUnit اختياري: إذا أُدخل، يُستخدم لاحقاً لحساب الربح الفعلي في التقارير.
 */
class StockOutUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        materialId: Long,
        quantity: Double,
        sellingPricePerUnit: Double? = null,
        note: String = ""
    ): OperationResult<Material> {
        if (quantity <= 0.0) {
            return OperationResult.Failure("الكمية يجب أن تكون أكبر من صفر")
        }
        if (sellingPricePerUnit != null && sellingPricePerUnit < 0.0) {
            return OperationResult.Failure("سعر البيع لا يمكن أن يكون سالباً")
        }

        val material = materialRepository.getMaterialById(materialId)
            ?: return OperationResult.Failure("المادة غير موجودة")

        if (material.currentQuantity < quantity) {
            return OperationResult.Failure(
                "الكمية المتوفرة غير كافية (المتوفر: ${material.currentQuantity} ${material.unit})"
            )
        }

        val updatedMaterial = material.copy(
            currentQuantity = material.currentQuantity - quantity
        )

        materialRepository.updateMaterial(updatedMaterial)

        transactionRepository.insertTransaction(
            StockTransaction(
                materialId = materialId,
                materialNameSnapshot = material.name,
                type = TransactionType.STOCK_OUT,
                quantity = quantity,
                unitCost = material.averageCost,
                sellingPricePerUnit = sellingPricePerUnit,
                note = note
            )
        )

        return OperationResult.Success(updatedMaterial)
    }
}
