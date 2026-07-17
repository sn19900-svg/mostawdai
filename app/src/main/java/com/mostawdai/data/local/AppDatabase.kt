package com.mostawdai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MaterialEntity::class, StockTransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao
    abstract fun transactionDao(): TransactionDao
}
