package com.mostawdai.domain.usecase

import com.mostawdai.domain.model.Material
import com.mostawdai.domain.repository.MaterialRepository
import com.mostawdai.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class InventorySummary(
    val materials: List<Material>,
    val totalInventoryValue: Double,
    val lowStockCount: Int,
    val totalPaidForMaterials: Double
)

/**
 * ملخص المخزون: قيمة المخزون الحالية، عدد المواد منخفضة المخزون،
 * والمبلغ الإجمالي الذي دُفع تاريخياً لشراء كل المواد (كل عمليات الإدخال منذ البداية).
 */
class GetInventorySummaryUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(): Flow<InventorySummary> {
        return combine(
            materialRepository.observeAllMaterials(),
            transactionRepository.observeTotalStockInCost()
        ) { materials, totalPaid ->
            InventorySummary(
                materials = materials,
                totalInventoryValue = materials.sumOf { it.totalValue },
                lowStockCount = materials.count { it.isLowStock },
                totalPaidForMaterials = totalPaid
            )
        }
    }
}
