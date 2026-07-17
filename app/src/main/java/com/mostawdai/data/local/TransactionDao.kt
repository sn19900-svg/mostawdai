package com.mostawdai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM stock_transactions WHERE materialId = :materialId ORDER BY date DESC")
    fun observeForMaterial(materialId: Long): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun observeInRange(startDate: Long, endDate: Long): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getAllInRange(startDate: Long, endDate: Long): List<StockTransactionEntity>

    @Insert
    suspend fun insert(transaction: StockTransactionEntity): Long
}
