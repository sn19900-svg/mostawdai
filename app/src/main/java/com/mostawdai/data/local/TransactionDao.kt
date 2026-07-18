package com.mostawdai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM stock_transactions WHERE materialId = :materialId ORDER BY date DESC")
    fun observeForMaterial(materialId: Long): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun observeInRange(startDate: Long, endDate: Long): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getAllInRange(startDate: Long, endDate: Long): List<StockTransactionEntity>

    @Query("""
        SELECT * FROM stock_transactions
        WHERE type = 'STOCK_OUT' AND sellingPricePerUnit IS NOT NULL
        AND date BETWEEN :startDate AND :endDate ORDER BY date ASC
    """)
    suspend fun getSalesInRange(startDate: Long, endDate: Long): List<StockTransactionEntity>

    @Query("SELECT * FROM stock_transactions ORDER BY date ASC")
    suspend fun getAllOnce(): List<StockTransactionEntity>

    @Query("SELECT * FROM stock_transactions WHERE id = :id")
    suspend fun getById(id: Long): StockTransactionEntity?

    @Insert
    suspend fun insert(transaction: StockTransactionEntity): Long

    @Update
    suspend fun update(transaction: StockTransactionEntity)

    @Query("DELETE FROM stock_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM stock_transactions")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(SUM(totalCost), 0) FROM stock_transactions WHERE type = 'STOCK_IN'")
    fun observeTotalStockInCost(): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(quantity * sellingPricePerUnit), 0) FROM stock_transactions
        WHERE type = 'STOCK_OUT' AND sellingPricePerUnit IS NOT NULL
    """)
    fun observeTotalSalesRevenue(): Flow<Double>
}
