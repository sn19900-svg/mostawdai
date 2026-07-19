package com.mostawdai.domain.repository

import com.mostawdai.domain.model.OperationResult
import java.io.File

data class ImportSummary(val materialsImported: Int, val transactionsImported: Int)

interface ExportRepository {
    suspend fun exportInventoryToExcel(): File
    suspend fun exportInventoryToPdf(): File
    suspend fun exportFullBackup(): File
    suspend fun importFullBackup(bytes: ByteArray): OperationResult<ImportSummary>
    suspend fun exportSalesReportToExcel(startDate: Long, endDate: Long): File
    suspend fun exportSalesReportToPdf(startDate: Long, endDate: Long): File
}
