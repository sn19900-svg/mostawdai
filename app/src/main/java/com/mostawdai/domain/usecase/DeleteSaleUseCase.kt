package com.mostawdai.domain.usecase

import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.model.TransactionType
import com.mostawdai.domain.repository.MaterialRepository
import com.mostawdai.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * حذف عملية بيع: يعيد الكمية المباعة إلى مخزون المادة (إن كانت المادة لا تزال موجودة) ثم يحذف السجل.
 */
class DeleteSaleUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: Long): OperationResult<Unit> {
        val transaction = transactionRepository.getTransactionById(transactionId)
            ?: return OperationResult.Failure("عملية البيع غير موجودة")

        if (transaction.type != TransactionType.STOCK_OUT) {
            return OperationResult.Failure("يمكن حذف عمليات التخريج/البيع فقط")
        }

        materialRepository.getMaterialById(transaction.materialId)?.let { material ->
            materialRepository.updateMaterial(
                material.copy(currentQuantity = material.currentQuantity + transaction.quantity)
            )
        }

        transactionRepository.deleteTransaction(transactionId)
        return OperationResult.Success(Unit)
    }
}
