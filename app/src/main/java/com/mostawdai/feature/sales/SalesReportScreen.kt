package com.mostawdai.feature.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostawdai.core.util.ShareUtils
import com.mostawdai.domain.model.StockTransaction
import com.mostawdai.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen(
    onBack: () -> Unit,
    viewModel: SalesReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    LaunchedEffect(state.fileToShare) {
        state.fileToShare?.let { file ->
            ShareUtils.shareFile(context, file, state.shareMimeType ?: "*/*", "مشاركة تقرير المبيعات")
            viewModel.consumeShareFile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نظام المبيعات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { showStartPicker = true }, modifier = Modifier.weight(1f)) {
                    Text("من: ${dateFormat.format(Date(state.startDate))}")
                }
                OutlinedButton(onClick = { showEndPicker = true }, modifier = Modifier.weight(1f)) {
                    Text("إلى: ${dateFormat.format(Date(state.endDate))}")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.report?.let { report ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("إجمالي المبيعات (الإيراد)", report.totalRevenue)
                        SummaryRow("تكلفة البضاعة المباعة", report.totalStockOutCost)
                        SummaryRow("صافي الربح", report.totalProfit, highlight = true)
                        HorizontalDivider()
                        SummaryRow("إجمالي المشتريات (إدخال)", report.totalStockInCost)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = viewModel::exportExcel, modifier = Modifier.weight(1f)) {
                        Text("تصدير Excel")
                    }
                    Button(onClick = viewModel::exportPdf, modifier = Modifier.weight(1f)) {
                        Text("تصدير PDF")
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("عمليات البيع خلال الفترة", style = MaterialTheme.typography.titleMedium)

                val salesOnly = remember(report.transactions) {
                    report.transactions.filter {
                        it.type == TransactionType.STOCK_OUT && it.sellingPricePerUnit != null
                    }
                }

                if (salesOnly.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "لا توجد مبيعات مسجّلة في هذه الفترة",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(salesOnly, key = { it.id }) { tx ->
                            SalesTransactionRow(tx)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.startDate)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.setStartDate(localStartOfDay(it)) }
                    showStartPicker = false
                }) { Text("موافق") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("إلغاء") } }
        ) { DatePicker(state = pickerState) }
    }

    if (showEndPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.endDate)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.setEndDate(localEndOfDay(it)) }
                    showEndPicker = false
                }) { Text("موافق") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("إلغاء") } }
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun SummaryRow(label: String, value: Double, highlight: Boolean = false) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            "%.2f".format(value),
            style = if (highlight) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = if (highlight) {
                if (value >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            } else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SalesTransactionRow(tx: StockTransaction) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar")) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(tx.materialNameSnapshot, style = MaterialTheme.typography.titleMedium)
            Text(dateFormat.format(Date(tx.date)), style = MaterialTheme.typography.bodyMedium)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("%.2f".format(tx.quantity))
            tx.profit?.let {
                Text(
                    "ربح: %.2f".format(it),
                    color = if (it >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun localStartOfDay(utcMillis: Long): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utcCal.timeInMillis = utcMillis
    val localCal = Calendar.getInstance()
    localCal.set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
    localCal.set(Calendar.MILLISECOND, 0)
    return localCal.timeInMillis
}

private fun localEndOfDay(utcMillis: Long): Long = localStartOfDay(utcMillis) + 24L * 60 * 60 * 1000 - 1
