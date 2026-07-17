package com.mostawdai.domain.usecase

import com.mostawdai.domain.model.Material
import com.mostawdai.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class InventorySummary(
    val materials: List<Material>,
    val totalInventoryValue: Double,
    val lowStockCount: Int
)

/**
 * ملخص المخزون الكامل: قيمة المخزون الإجمالية (مجموع كمية×متوسط تكلفة لكل مادة) + عدد المواد منخفضة المخزون.
 */
class GetInventorySummaryUseCase @Inject constructor(
    private val materialRepository: MaterialRepository
) {
    operator fun invoke(): Flow<InventorySummary> {
        return materialRepository.observeAllMaterials().map { materials ->
            InventorySummary(
                materials = materials,
                totalInventoryValue = materials.sumOf { it.totalValue },
                lowStockCount = materials.count { it.isLowStock }
            )
        }
    }
}
