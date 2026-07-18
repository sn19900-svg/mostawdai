package com.mostawdai.domain.usecase

import com.mostawdai.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class ProfitOverview(
    val totalPaidForMaterials: Double,
    val totalSalesRevenue: Double,
    val netProfit: Double
)

/**
 * الأرباح = إجمالي إيراد المبيعات (كل عمليات التخريج التي سُجِّل فيها سعر بيع) 
 * ناقص إجمالي المبلغ المدفوع لشراء المواد (كل عمليات الإدخال منذ البداية).
 * قيمة موجبة = ربح، قيمة سالبة = خسارة.
 */
class GetProfitOverviewUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(): Flow<ProfitOverview> {
        return combine(
            transactionRepository.observeTotalStockInCost(),
            transactionRepository.observeTotalSalesRevenue()
        ) { totalPaid, totalRevenue ->
            ProfitOverview(
                totalPaidForMaterials = totalPaid,
                totalSalesRevenue = totalRevenue,
                netProfit = totalRevenue - totalPaid
            )
        }
    }
}
