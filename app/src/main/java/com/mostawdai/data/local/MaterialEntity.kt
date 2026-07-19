package com.mostawdai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncId: String = "",
    val materialNumber: String = "",
    val name: String,
    val unit: String,
    val currentQuantity: Double,
    val averageCost: Double,
    val minQuantityAlert: Double,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long = 0
)
