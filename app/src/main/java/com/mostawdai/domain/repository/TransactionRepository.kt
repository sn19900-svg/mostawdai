package com.mostawdai.domain.repository

import com.mostawdai.domain.model.StockTransaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactionsForMaterial(materialId: Long): Flow<List<StockTransaction>>
    fun observeTransactionsInRange(startDate: Long, endDate: Long): Flow<List<StockTransaction>>
    suspend fun insertTransaction(transaction: StockTransaction): Long
    suspend fun getAllTransactionsInRange(startDate: Long, endDate: Long): List<StockTransaction>
}
