package com.mostawdai.domain.usecase

import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.model.TransactionType
import com.mostawdai.domain.repository.MaterialRepository
import com.mostawdai.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * تعديل عملية بيع موجودة (الكمية، سعر البيع، الملاحظة).
 * سعر التكلفة (unitCost) يبقى ثابتاً كما كان وقت العملية (لا يُعاد حسابه)، حفاظاً على دقة السجل التاريخي.
 * أي فرق في الكمية يُعدَّل مباشرة على كمية المادة الحالية.
 */
class UpdateSaleUseCase @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        transactionId: Long,
        newQuantity: Double,
        newSellingPricePerUnit: Double?,
        newNote: String
    ): OperationResult<Unit> {
        if (newQuantity <= 0.0) return OperationResult.Failure("الكمية يجب أن تكون أكبر من صفر")
        if (newSellingPricePerUnit != null && newSellingPricePerUnit < 0.0) {
            return OperationResult.Failure("سعر البيع لا يمكن أن يكون سالباً")
        }

        val transaction = transactionRepository.getTransactionById(transactionId)
            ?: return OperationResult.Failure("عملية البيع غير موجودة")

        if (transaction.type != TransactionType.STOCK_OUT) {
            return OperationResult.Failure("يمكن تعديل عمليات التخريج/البيع فقط")
        }

        val material = materialRepository.getMaterialById(transaction.materialId)
            ?: return OperationResult.Failure("المادة المرتبطة بهذه العملية لم تعد موجودة")

        val quantityDelta = newQuantity - transaction.quantity
        if (quantityDelta > 0 && material.currentQuantity < quantityDelta) {
            return OperationResult.Failure("الكمية المتوفرة غير كافية لزيادة كمية هذه العملية")
        }

        materialRepository.updateMaterial(
            material.copy(currentQuantity = material.currentQuantity - quantityDelta)
        )

        transactionRepository.updateTransaction(
            transaction.copy(
                quantity = newQuantity,
                totalCost = newQuantity * transaction.unitCost,
                sellingPricePerUnit = newSellingPricePerUnit,
                note = newNote
            )
        )

        return OperationResult.Success(Unit)
    }
}
