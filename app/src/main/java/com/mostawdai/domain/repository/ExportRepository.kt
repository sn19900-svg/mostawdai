package com.mostawdai.domain.repository

import com.mostawdai.domain.model.OperationResult
import java.io.File
import java.io.InputStream

data class ImportSummary(val materialsImported: Int, val transactionsImported: Int)

interface ExportRepository {
    suspend fun exportInventoryToExcel(): File
    suspend fun exportInventoryToPdf(): File
    suspend fun exportFullBackup(): File
    suspend fun importFullBackup(inputStream: InputStream): OperationResult<ImportSummary>
}
