package com.mostawdai.domain.usecase

import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.repository.MaterialRepository
import javax.inject.Inject

/**
 * تعديل بيانات مادة موجودة، بما فيها تصحيح يدوي للكمية الحالية ومتوسط التكلفة.
 * ملاحظة: التعديل اليدوي للكمية/التكلفة لا يُنشئ سجل حركة في السجل التاريخي،
 * لأنه تصحيح مباشر وليس عملية إدخال/تخريج فعلية.
 */
class UpdateMaterialUseCase @Inject constructor(
    private val materialRepository: MaterialRepository
) {
    suspend operator fun invoke(
        materialId: Long,
        name: String,
        unit: String,
        currentQuantity: Double,
        averageCost: Double,
        minQuantityAlert: Double,
        notes: String
    ): OperationResult<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return OperationResult.Failure("اسم المادة مطلوب")
        if (unit.trim().isEmpty()) return OperationResult.Failure("وحدة القياس مطلوبة")
        if (currentQuantity < 0.0) return OperationResult.Failure("الكمية لا يمكن أن تكون سالبة")
        if (averageCost < 0.0) return OperationResult.Failure("متوسط التكلفة لا يمكن أن يكون سالباً")

        if (materialRepository.materialNameExists(trimmedName, excludeId = materialId)) {
            return OperationResult.Failure("يوجد مادة أخرى بنفس الاسم")
        }

        val existing = materialRepository.getMaterialById(materialId)
            ?: return OperationResult.Failure("المادة غير موجودة")

        materialRepository.updateMaterial(
            existing.copy(
                name = trimmedName,
                unit = unit.trim(),
                currentQuantity = currentQuantity,
                averageCost = averageCost,
                minQuantityAlert = minQuantityAlert,
                notes = notes.trim()
            )
        )
        return OperationResult.Success(Unit)
    }
}
