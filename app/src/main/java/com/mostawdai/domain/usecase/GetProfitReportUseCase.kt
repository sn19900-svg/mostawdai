package com.mostawdai.domain.usecase

import com.mostawdai.domain.model.StockTransaction
import com.mostawdai.domain.model.TransactionType
import com.mostawdai.domain.repository.TransactionRepository
import javax.inject.Inject

data class ProfitReport(
    val startDate: Long,
    val endDate: Long,
    val totalStockInCost: Double,   // إجمالي تكلفة المشتريات في الفترة
    val totalStockOutCost: Double,  // إجمالي تكلفة المواد المخرجة (تكلفة البضاعة المستخدمة) في الفترة
    val totalRevenue: Double,       // إجمالي الإيراد (فقط من عمليات التخريج التي فيها سعر بيع مسجّل)
    val totalProfit: Double,        // إجمالي الربح = totalRevenue - (تكلفة العمليات التي فيها سعر بيع فقط)
    val transactions: List<StockTransaction>
)

/**
 * تقرير لفترة زمنية (يومي/شهري/سنوي حسب التواريخ الممررة).
 * الربح يُحسب فقط من عمليات التخريج التي سُجِّل فيها سعر بيع؛ عمليات التخريج بدون سعر بيع
 * (استهلاك داخلي مثلاً) تُحتسب ضمن totalStockOutCost لكن لا تدخل في حساب الربح.
 */
class GetProfitReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(startDate: Long, endDate: Long): ProfitReport {
        val transactions = transactionRepository.getAllTransactionsInRange(startDate, endDate)
        val stockIn = transactions.filter { it.type == TransactionType.STOCK_IN }
        val stockOut = transactions.filter { it.type == TransactionType.STOCK_OUT }
        val soldOut = stockOut.filter { it.sellingPricePerUnit != null }

        val totalIn = stockIn.sumOf { it.totalCost }
        val totalOut = stockOut.sumOf { it.totalCost }
        val totalRevenue = soldOut.sumOf { it.totalRevenue ?: 0.0 }
        val totalProfit = soldOut.sumOf { it.profit ?: 0.0 }

        return ProfitReport(
            startDate = startDate,
            endDate = endDate,
            totalStockInCost = totalIn,
            totalStockOutCost = totalOut,
            totalRevenue = totalRevenue,
            totalProfit = totalProfit,
            transactions = transactions
        )
    }
}
