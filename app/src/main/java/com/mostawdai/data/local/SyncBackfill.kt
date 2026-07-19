package com.mostawdai.data.local

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncBackfill @Inject constructor(
    private val materialDao: MaterialDao,
    private val transactionDao: TransactionDao
) {
    suspend fun run() {
        materialDao.getAllOnce().forEach { material ->
            if (material.syncId.isBlank() || material.updatedAt == 0L) {
                materialDao.update(
                    material.copy(
                        syncId = material.syncId.ifBlank { UUID.randomUUID().toString() },
                        updatedAt = if (material.updatedAt == 0L) material.createdAt else material.updatedAt
                    )
                )
            }
        }
        transactionDao.getAllOnce().forEach { tx ->
            if (tx.syncId.isBlank() || tx.updatedAt == 0L) {
                transactionDao.update(
                    tx.copy(
                        syncId = tx.syncId.ifBlank { UUID.randomUUID().toString() },
                        updatedAt = if (tx.updatedAt == 0L) tx.date else tx.updatedAt
                    )
                )
            }
        }
    }
}
