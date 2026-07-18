package com.mostawdai.data.repository

import com.mostawdai.data.local.TransactionDao
import com.mostawdai.data.local.toDomain
import com.mostawdai.data.local.toEntity
import com.mostawdai.domain.model.StockTransaction
import com.mostawdai.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun observeTransactionsForMaterial(materialId: Long): Flow<List<StockTransaction>> =
        dao.observeForMaterial(materialId).map { list -> list.map { it.toDomain() } }

    override fun observeTransactionsInRange(startDate: Long, endDate: Long): Flow<List<StockTransaction>> =
        dao.observeInRange(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override suspend fun insertTransaction(transaction: StockTransaction): Long =
        dao.insert(transaction.toEntity())

    override suspend fun getAllTransactionsInRange(startDate: Long, endDate: Long): List<StockTransaction> =
        dao.getAllInRange(startDate, endDate).map { it.toDomain() }

    override suspend fun getTransactionById(id: Long): StockTransaction? =
        dao.getById(id)?.toDomain()

    override suspend fun updateTransaction(transaction: StockTransaction) =
        dao.update(transaction.toEntity())

    override suspend fun deleteTransaction(id: Long) =
        dao.deleteById(id)

    override fun observeTotalStockInCost(): Flow<Double> = dao.observeTotalStockInCost()

    override fun observeTotalSalesRevenue(): Flow<Double> = dao.observeTotalSalesRevenue()
}
