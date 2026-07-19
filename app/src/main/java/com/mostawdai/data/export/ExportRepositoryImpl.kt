package com.mostawdai.data.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.mostawdai.data.local.MaterialDao
import com.mostawdai.data.local.MaterialEntity
import com.mostawdai.data.local.StockTransactionEntity
import com.mostawdai.data.local.TransactionDao
import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.repository.ExportRepository
import com.mostawdai.domain.repository.ImportSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhatim.fastexcel.Workbook
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val materialDao: MaterialDao,
    private val transactionDao: TransactionDao
) : ExportRepository {

    private fun exportsDir(): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    // ---------- Excel: المخزون الكامل ----------

    override suspend fun exportInventoryToExcel(): File = withContext(Dispatchers.IO) {
        val materials = materialDao.getAllOnce()
        val transactions = transactionDao.getAllOnce()

        val file = File(exportsDir(), "mostawdai_inventory_${timestamp()}.xlsx")
        FileOutputStream(file).use { fos ->
            val wb = Workbook(fos, "مستودعي", "1.0")

            val materialsSheet = wb.newWorksheet("المواد")
            val materialHeaders = listOf("الاسم", "الوحدة", "الكمية الحالية", "متوسط التكلفة", "القيمة الإجمالية", "حد التنبيه")
            materialHeaders.forEachIndexed { col, header -> materialsSheet.value(0, col, header) }
            materials.forEachIndexed { index, m ->
                val row = index + 1
                materialsSheet.value(row, 0, m.name)
                materialsSheet.value(row, 1, m.unit)
                materialsSheet.value(row, 2, m.currentQuantity)
                materialsSheet.value(row, 3, m.averageCost)
                materialsSheet.value(row, 4, m.currentQuantity * m.averageCost)
                materialsSheet.value(row, 5, m.minQuantityAlert)
            }

            val transactionsSheet = wb.newWorksheet("الحركات")
            val txHeaders = listOf("التاريخ", "المادة", "النوع", "الكمية", "سعر الوحدة", "التكلفة الإجمالية", "سعر البيع", "ملاحظة")
            txHeaders.forEachIndexed { col, header -> transactionsSheet.value(0, col, header) }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            transactions.forEachIndexed { index, t ->
                val row = index + 1
                transactionsSheet.value(row, 0, dateFormat.format(Date(t.date)))
                transactionsSheet.value(row, 1, t.materialNameSnapshot)
                transactionsSheet.value(row, 2, if (t.type == "STOCK_IN") "إدخال" else "تخريج")
                transactionsSheet.value(row, 3, t.quantity)
                transactionsSheet.value(row, 4, t.unitCost)
                transactionsSheet.value(row, 5, t.totalCost)
                transactionsSheet.value(row, 6, t.sellingPricePerUnit ?: 0.0)
                transactionsSheet.value(row, 7, t.note)
            }

            wb.finish()
        }
        file
    }

    // ---------- PDF: تقرير المخزون ----------

    override suspend fun exportInventoryToPdf(): File = withContext(Dispatchers.IO) {
        val materials = materialDao.getAllOnce()
        val file = File(exportsDir(), "mostawdai_report_${timestamp()}.pdf")

        val pageWidth = 595
        val pageHeight = 842
        val marginLeft = 40f
        val lineHeight = 22f
        val document = PdfDocument()

        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val headerPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 11f }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas: Canvas = page.canvas
        var y = 50f

        canvas.drawText("تقرير مخزون - مستودعي", marginLeft, y, titlePaint)
        y += 20f
        canvas.drawText(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date()), marginLeft, y, bodyPaint)
        y += lineHeight * 1.5f

        canvas.drawText("الاسم | الوحدة | الكمية | متوسط التكلفة | القيمة", marginLeft, y, headerPaint)
        y += lineHeight

        var totalValue = 0.0
        for (m in materials) {
            if (y > pageHeight - 60f) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = 50f
            }
            val value = m.currentQuantity * m.averageCost
            totalValue += value
            val line = "%s | %s | %.2f | %.2f | %.2f".format(m.name, m.unit, m.currentQuantity, m.averageCost, value)
            canvas.drawText(line, marginLeft, y, bodyPaint)
            y += lineHeight
        }

        y += lineHeight
        canvas.drawText("إجمالي قيمة المخزون: %.2f".format(totalValue), marginLeft, y, headerPaint)

        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        file
    }

    // ---------- Excel: تقرير المبيعات لفترة ----------

    override suspend fun exportSalesReportToExcel(startDate: Long, endDate: Long): File = withContext(Dispatchers.IO) {
        val sales = transactionDao.getSalesInRange(startDate, endDate)
        val file = File(exportsDir(), "mostawdai_sales_${timestamp()}.xlsx")
        FileOutputStream(file).use { fos ->
            val wb = Workbook(fos, "مستودعي", "1.0")
            val sheet = wb.newWorksheet("المبيعات")
            val headers = listOf("التاريخ", "المادة", "الكمية", "التكلفة للوحدة", "سعر البيع", "الإيراد", "الربح", "ملاحظة")
            headers.forEachIndexed { col, h -> sheet.value(0, col, h) }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            var totalRevenue = 0.0
            var totalCost = 0.0
            var totalProfit = 0.0

            sales.forEachIndexed { index, t ->
                val row = index + 1
                val revenue = (t.sellingPricePerUnit ?: 0.0) * t.quantity
                val profit = revenue - t.totalCost
                totalRevenue += revenue
                totalCost += t.totalCost
                totalProfit += profit

                sheet.value(row, 0, dateFormat.format(Date(t.date)))
                sheet.value(row, 1, t.materialNameSnapshot)
                sheet.value(row, 2, t.quantity)
                sheet.value(row, 3, t.unitCost)
                sheet.value(row, 4, t.sellingPricePerUnit ?: 0.0)
                sheet.value(row, 5, revenue)
                sheet.value(row, 6, profit)
                sheet.value(row, 7, t.note)
            }

            val totalsRow = sales.size + 2
            sheet.value(totalsRow, 1, "الإجمالي")
            sheet.value(totalsRow, 5, totalRevenue)
            sheet.value(totalsRow, 6, totalProfit)

            wb.finish()
        }
        file
    }

    // ---------- PDF: تقرير المبيعات لفترة ----------

    override suspend fun exportSalesReportToPdf(startDate: Long, endDate: Long): File = withContext(Dispatchers.IO) {
        val sales = transactionDao.getSalesInRange(startDate, endDate)
        val file = File(exportsDir(), "mostawdai_sales_report_${timestamp()}.pdf")

        val pageWidth = 595
        val pageHeight = 842
        val marginLeft = 40f
        val lineHeight = 22f
        val document = PdfDocument()

        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val headerPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 11f }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas: Canvas = page.canvas
        var y = 50f

        val periodFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        canvas.drawText("تقرير المبيعات - مستودعي", marginLeft, y, titlePaint)
        y += 20f
        canvas.drawText(
            "من ${periodFormat.format(Date(startDate))} إلى ${periodFormat.format(Date(endDate))}",
            marginLeft, y, bodyPaint
        )
        y += lineHeight * 1.5f

        canvas.drawText("التاريخ | المادة | الكمية | التكلفة | سعر البيع | الربح", marginLeft, y, headerPaint)
        y += lineHeight

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        var totalRevenue = 0.0
        var totalCost = 0.0
        var totalProfit = 0.0

        for (t in sales) {
            if (y > pageHeight - 90f) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = 50f
            }
            val revenue = (t.sellingPricePerUnit ?: 0.0) * t.quantity
            val profit = revenue - t.totalCost
            totalRevenue += revenue
            totalCost += t.totalCost
            totalProfit += profit

            val line = "%s | %s | %.2f | %.2f | %.2f | %.2f".format(
                dateFormat.format(Date(t.date)), t.materialNameSnapshot, t.quantity,
                t.totalCost, t.sellingPricePerUnit ?: 0.0, profit
            )
            canvas.drawText(line, marginLeft, y, bodyPaint)
            y += lineHeight
        }

        y += lineHeight
        canvas.drawText("إجمالي الإيراد: %.2f".format(totalRevenue), marginLeft, y, headerPaint)
        y += lineHeight
        canvas.drawText("إجمالي التكلفة: %.2f".format(totalCost), marginLeft, y, headerPaint)
        y += lineHeight
        canvas.drawText("صافي الربح: %.2f".format(totalProfit), marginLeft, y, headerPaint)

        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        file
    }

    // ---------- نسخة احتياطية كاملة (JSON) ----------

    override suspend fun exportFullBackup(): File = withContext(Dispatchers.IO) {
        val materials = materialDao.getAllOnce()
        val transactions = transactionDao.getAllOnce()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val materialsArray = JSONArray()
        materials.forEach { m ->
            materialsArray.put(JSONObject().apply {
                put("name", m.name)
                put("unit", m.unit)
                put("currentQuantity", m.currentQuantity)
                put("averageCost", m.averageCost)
                put("minQuantityAlert", m.minQuantityAlert)
                put("notes", m.notes)
                put("createdAt", m.createdAt)
            })
        }
        root.put("materials", materialsArray)

        val transactionsArray = JSONArray()
        transactions.forEach { t ->
            transactionsArray.put(JSONObject().apply {
                put("materialNameSnapshot", t.materialNameSnapshot)
                put("type", t.type)
                put("quantity", t.quantity)
                put("unitCost", t.unitCost)
                put("totalCost", t.totalCost)
                put("sellingPricePerUnit", t.sellingPricePerUnit ?: JSONObject.NULL)
                put("note", t.note)
                put("date", t.date)
            })
        }
        root.put("transactions", transactionsArray)

        val file = File(exportsDir(), "mostawdai_backup_${timestamp()}.json")
        file.writeText(root.toString(2))
        file
    }

    override suspend fun importFullBackup(bytes: ByteArray): OperationResult<ImportSummary> =
        withContext(Dispatchers.IO) {
            try {
                val text = String(bytes, Charsets.UTF_8)
                val root = JSONObject(text)

                if (!root.has("materials") || !root.has("transactions")) {
                    return@withContext OperationResult.Failure("الملف غير صالح: تنسيق نسخة احتياطية غير معروف")
                }

                transactionDao.deleteAll()
                materialDao.deleteAll()

                val nameToNewId = mutableMapOf<String, Long>()
                val materialsArray = root.getJSONArray("materials")
                for (i in 0 until materialsArray.length()) {
                    val obj = materialsArray.getJSONObject(i)
                    val entity = MaterialEntity(
                        name = obj.getString("name"),
                        unit = obj.getString("unit"),
                        currentQuantity = obj.getDouble("currentQuantity"),
                        averageCost = obj.getDouble("averageCost"),
                        minQuantityAlert = obj.optDouble("minQuantityAlert", 0.0),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                    val newId = materialDao.insert(entity)
                    nameToNewId[entity.name] = newId
                }

                var transactionsImported = 0
                val transactionsArray = root.getJSONArray("transactions")
                for (i in 0 until transactionsArray.length()) {
                    val obj = transactionsArray.getJSONObject(i)
                    val materialName = obj.getString("materialNameSnapshot")
                    val matchedId = nameToNewId[materialName]
                    val entity = StockTransactionEntity(
                        materialId = matchedId,
                        materialNameSnapshot = materialName,
                        type = obj.getString("type"),
                        quantity = obj.getDouble("quantity"),
                        unitCost = obj.getDouble("unitCost"),
                        totalCost = obj.getDouble("totalCost"),
                        sellingPricePerUnit = if (obj.isNull("sellingPricePerUnit")) null else obj.getDouble("sellingPricePerUnit"),
                        note = obj.optString("note", ""),
                        date = obj.getLong("date")
                    )
                    transactionDao.insert(entity)
                    transactionsImported++
                }

                OperationResult.Success(
                    ImportSummary(
                        materialsImported = nameToNewId.size,
                        transactionsImported = transactionsImported
                    )
                )
            } catch (e: Exception) {
                OperationResult.Failure("فشل استيراد الملف: ${e.message ?: "خطأ غير معروف"}")
            }
        }
}
