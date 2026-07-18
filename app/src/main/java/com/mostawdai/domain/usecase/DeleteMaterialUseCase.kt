package com.mostawdai.domain.usecase

import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.repository.MaterialRepository
import javax.inject.Inject

/**
 * حذف مادة. سجل الحركات التاريخية الخاصة بها يبقى محفوظاً (مع اسم المادة كما كان)
 * لكن يصبح غير مرتبط بأي مادة حالية، بفضل ON DELETE SET NULL في قاعدة البيانات.
 */
class DeleteMaterialUseCase @Inject constructor(
    private val materialRepository: MaterialRepository
) {
    suspend operator fun invoke(materialId: Long): OperationResult<Unit> {
        materialRepository.deleteMaterial(materialId)
        return OperationResult.Success(Unit)
    }
}
