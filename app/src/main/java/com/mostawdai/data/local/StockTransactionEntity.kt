package com.mostawdai.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_transactions",
    foreignKeys = [
        ForeignKey(
            entity = MaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["materialId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("materialId"), Index("date")]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val materialId: Long?,
    val materialNameSnapshot: String,
    val type: String, // "STOCK_IN" أو "STOCK_OUT"
    val quantity: Double,
    val unitCost: Double,
    val totalCost: Double,
    val sellingPricePerUnit: Double?,
    val note: String,
    val date: Long
)
