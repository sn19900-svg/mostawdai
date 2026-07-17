package com.mostawdai.domain.usecase

import com.mostawdai.domain.model.Material
import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.model.StockTransaction
import com.mostawdai.domain.model.TransactionType
import com.mostawdai.domain.repository.MaterialRepository
import com.mostawdai.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * إدخال كمية جديدة من مادة (شراء/توريد)، مع إعادة حساب متوسط التكلفة المرجّح تلقائياً:
 * المتوسط الجديد = (الكمية الحالية × متوسط التكلفة الحالي + الكمية الجديدة × سعر الشراء) ÷ (الكمية الحالية + الكمية الجديدة)
 */
class StockInUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        materialId: Long,
        quantity: Double,
        unitCost: Double,
        note: String = ""
    ): OperationResult<Material> {
        if (quantity <= 0.0) {
            return OperationResult.Failure("الكمية يجب أن تكون أكبر من صفر")
        }
        if (unitCost < 0.0) {
            return OperationResult.Failure("سعر الشراء لا يمكن أن يكون سالباً")
        }

        val material = materialRepository.getMaterialById(materialId)
            ?: return OperationResult.Failure("المادة غير موجودة")

        val oldQty = material.currentQuantity
        val oldAvgCost = material.averageCost
        val newQty = oldQty + quantity

        val newAvgCost = if (newQty == 0.0) {
            0.0
        } else {
            ((oldQty * oldAvgCost) + (quantity * unitCost)) / newQty
        }

        val updatedMaterial = material.copy(
            currentQuantity = newQty,
            averageCost = newAvgCost
        )

        materialRepository.updateMaterial(updatedMaterial)

        transactionRepository.insertTransaction(
            StockTransaction(
                materialId = materialId,
                materialNameSnapshot = material.name,
                type = TransactionType.STOCK_IN,
                quantity = quantity,
                unitCost = unitCost,
                note = note
            )
        )

        return OperationResult.Success(updatedMaterial)
    }
}
