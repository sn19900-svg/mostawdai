package com.mostawdai.domain.usecase

import com.mostawdai.domain.model.Material
import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.repository.MaterialRepository
import javax.inject.Inject

class AddMaterialUseCase @Inject constructor(
    private val materialRepository: MaterialRepository
) {
    suspend operator fun invoke(
        name: String,
        unit: String,
        materialNumber: String = "",
        minQuantityAlert: Double = 0.0,
        notes: String = ""
    ): OperationResult<Long> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return OperationResult.Failure("اسم المادة مطلوب")
        }
        if (unit.trim().isEmpty()) {
            return OperationResult.Failure("وحدة القياس مطلوبة")
        }
        if (materialRepository.materialNameExists(trimmedName)) {
            return OperationResult.Failure("يوجد مادة أخرى بنفس الاسم")
        }

        val id = materialRepository.insertMaterial(
            Material(
                name = trimmedName,
                unit = unit.trim(),
                materialNumber = materialNumber.trim(),
                minQuantityAlert = minQuantityAlert,
                notes = notes.trim()
            )
        )
        return OperationResult.Success(id)
    }
}
