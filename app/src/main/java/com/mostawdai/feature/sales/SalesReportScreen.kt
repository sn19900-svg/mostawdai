package com.mostawdai.feature.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostawdai.core.util.ShareUtils
import com.mostawdai.domain.model.Material
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
    val materials by viewModel.materials.collectAsState()
    val context = LocalContext.current
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var showAddSaleDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<StockTransaction?>(null) }
    var deletingTransactionId by remember { mutableStateOf<Long?>(null) }
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
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "رجوع")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.Share, contentDescription = "تصدير التقرير")
                        }
                        DropdownMenu(expanded = showExportMenu, onDismissRequest = { showExportMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("تصدير Excel") },
                                onClick = { showExportMenu = false; viewModel.exportExcel() }
                            )
                            DropdownMenuItem(
                                text = { Text("تصدير PDF") },
                                onClick = { showExportMenu = false; viewModel.exportPdf() }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSaleDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "تسجيل عملية بيع")
            }
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

            state.errorMessage?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            state.report?.let { report ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("إجمالي المبيعات (الإيراد)", report.totalRevenue)
                        SummaryRow("تكلفة البضاعة المباعة", report.totalStockOutCost)
                        SummaryRow("صافي الربح", report.totalProfit, highlight = true)
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
                            SalesTransactionRow(
                                tx = tx,
                                onEdit = { editingTransaction = tx },
                                onDelete = { deletingTransactionId = tx.id }
                            )
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

    if (showAddSaleDialog) {
        AddSaleDialog(
            materials = materials,
            onDismiss = { showAddSaleDialog = false },
            onConfirm = { materialId, quantity, price, note ->
                viewModel.addSale(materialId, quantity, price, note)
                showAddSaleDialog = false
            }
        )
    }

    editingTransaction?.let { tx ->
        EditSaleDialog(
            transaction = tx,
            onDismiss = { editingTransaction = null },
            onConfirm = { quantity, price, note ->
                viewModel.updateSale(tx.id, quantity, price, note)
                editingTransaction = null
            }
        )
    }

    deletingTransactionId?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingTransactionId = null },
            title = { Text("حذف عملية البيع") },
            text = { Text("سيُعاد إدراج الكمية المباعة إلى المخزون. متابعة؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSale(id)
                    deletingTransactionId = null
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingTransactionId = null }) { Text("إلغاء") }
            }
        )
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
private fun SalesTransactionRow(
    tx: StockTransaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar")) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.materialNameSnapshot, style = MaterialTheme.typography.titleMedium)
            Text(dateFormat.format(Date(tx.date)), style = MaterialTheme.typography.bodyMedium)
            Text(
                "%.2f بسعر %.2f".format(tx.quantity, tx.sellingPricePerUnit ?: 0.0),
                style = MaterialTheme.typography.bodyMedium
            )
            tx.profit?.let {
                Text(
                    "ربح: %.2f".format(it),
                    color = if (it >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
        Row {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "تعديل")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSaleDialog(
    materials: List<Material>,
    onDismiss: () -> Unit,
    onConfirm: (materialId: Long, quantity: Double, sellingPrice: Double, note: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<Material?>(null) }
    var quantity by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل عملية بيع") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedMaterial?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المادة") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        materials.forEach { material ->
                            DropdownMenuItem(
                                text = { Text("${material.name} (متوفر: %.2f %s)".format(material.currentQuantity, material.unit)) },
                                onClick = { selectedMaterial = material; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("الكمية المباعة (${selectedMaterial?.unit ?: ""})") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("سعر البيع للوحدة") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة (اختياري)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val material = selectedMaterial
                val q = quantity.toDoubleOrNull()
                val p = sellingPrice.toDoubleOrNull()
                if (material != null && q != null && p != null) {
                    onConfirm(material.id, q, p, note)
                }
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun EditSaleDialog(
    transaction: StockTransaction,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, sellingPrice: Double, note: String) -> Unit
) {
    var quantity by remember { mutableStateOf(transaction.quantity.toString()) }
    var sellingPrice by remember { mutableStateOf((transaction.sellingPricePerUnit ?: 0.0).toString()) }
    var note by remember { mutableStateOf(transaction.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل عملية البيع") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(transaction.materialNameSnapshot, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = quantity, onValueChange = { quantity = it },
                    label = { Text("الكمية") }, singleLine = true
                )
                OutlinedTextField(
                    value = sellingPrice, onValueChange = { sellingPrice = it },
                    label = { Text("سعر البيع للوحدة") }, singleLine = true
                )
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("ملاحظة") }, singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val q = quantity.toDoubleOrNull()
                val p = sellingPrice.toDoubleOrNull()
                if (q != null && p != null) onConfirm(q, p, note)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
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
