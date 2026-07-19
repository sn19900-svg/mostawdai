package com.mostawdai.data.local

import com.mostawdai.domain.model.Material
import com.mostawdai.domain.model.StockTransaction
import com.mostawdai.domain.model.TransactionType

fun MaterialEntity.toDomain(): Material = Material(
    id = id,
    materialNumber = materialNumber,
    name = name,
    unit = unit,
    currentQuantity = currentQuantity,
    averageCost = averageCost,
    minQuantityAlert = minQuantityAlert,
    notes = notes,
    createdAt = createdAt
)

fun Material.toEntity(): MaterialEntity = MaterialEntity(
    id = id,
    materialNumber = materialNumber,
    name = name,
    unit = unit,
    currentQuantity = currentQuantity,
    averageCost = averageCost,
    minQuantityAlert = minQuantityAlert,
    notes = notes,
    createdAt = createdAt
)

fun StockTransactionEntity.toDomain(): StockTransaction = StockTransaction(
    id = id,
    materialId = materialId ?: 0L,
    materialNameSnapshot = materialNameSnapshot,
    type = TransactionType.valueOf(type),
    quantity = quantity,
    unitCost = unitCost,
    totalCost = totalCost,
    sellingPricePerUnit = sellingPricePerUnit,
    note = note,
    date = date
)

fun StockTransaction.toEntity(): StockTransactionEntity = StockTransactionEntity(
    id = id,
    materialId = materialId,
    materialNameSnapshot = materialNameSnapshot,
    type = type.name,
    quantity = quantity,
    unitCost = unitCost,
    totalCost = totalCost,
    sellingPricePerUnit = sellingPricePerUnit,
    note = note,
    date = date
)
