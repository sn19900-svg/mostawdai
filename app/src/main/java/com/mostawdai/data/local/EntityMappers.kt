package com.mostawdai.data.local

import com.mostawdai.domain.model.Material
import com.mostawdai.domain.model.StockTransaction
import com.mostawdai.domain.model.TransactionType

fun MaterialEntity.toDomain(): Material = Material(
    id = id,
    syncId = syncId,
    materialNumber = materialNumber,
    name = name,
    unit = unit,
    currentQuantity = currentQuantity,
    averageCost = averageCost,
    minQuantityAlert = minQuantityAlert,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Material.toEntity(): MaterialEntity = MaterialEntity(
    id = id,
    syncId = syncId,
    materialNumber = materialNumber,
    name = name,
    unit = unit,
    currentQuantity = currentQuantity,
    averageCost = averageCost,
    minQuantityAlert = minQuantityAlert,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun StockTransactionEntity.toDomain(): StockTransaction = StockTransaction(
    id = id,
    syncId = syncId,
    materialId = materialId ?: 0L,
    materialNameSnapshot = materialNameSnapshot,
    type = TransactionType.valueOf(type),
    quantity = quantity,
    unitCost = unitCost,
    totalCost = totalCost,
    sellingPricePerUnit = sellingPricePerUnit,
    note = note,
    date = date,
    updatedAt = updatedAt
)

fun StockTransaction.toEntity(): StockTransactionEntity = StockTransactionEntity(
    id = id,
    syncId = syncId,
    materialId = materialId,
    materialNameSnapshot = materialNameSnapshot,
    type = type.name,
    quantity = quantity,
    unitCost = unitCost,
    totalCost = totalCost,
    sellingPricePerUnit = sellingPricePerUnit,
    note = note,
    date = date,
    updatedAt = updatedAt
)
