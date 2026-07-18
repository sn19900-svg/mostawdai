package com.mostawdai.feature.materials

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostawdai.domain.model.StockTransaction
import com.mostawdai.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailScreen(
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: MaterialDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showStockInDialog by remember { mutableStateOf(false) }
    var showStockOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.material?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل المادة")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            state.material?.let { material ->
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("الكمية الحالية", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "%.2f %s".format(material.currentQuantity, material.unit),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("متوسط التكلفة", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "%.2f".format(material.averageCost),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "القيمة الإجمالية: %.2f".format(material.totalValue),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showStockInDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isProcessing
                    ) { Text("إدخال") }

                    OutlinedButton(
                        onClick = { showStockOutDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isProcessing
                    ) { Text("تخريج") }
                }

                state.errorMessage?.let { error ->
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "سجل الحركات",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                items(state.transactions, key = { it.id }) { transaction ->
                    TransactionRow(transaction)
                    HorizontalDivider()
                }
            }
        }
    }

    if (showStockInDialog) {
        StockInDialog(
            unit = state.material?.unit ?: "",
            onDismiss = { showStockInDialog = false },
            onConfirm = { qty, cost, note ->
                viewModel.stockIn(qty, cost, note)
                showStockInDialog = false
            }
        )
    }

    if (showStockOutDialog) {
        StockOutDialog(
            unit = state.material?.unit ?: "",
            maxQuantity = state.material?.currentQuantity ?: 0.0,
            onDismiss = { showStockOutDialog = false },
            onConfirm = { qty, price, note ->
                viewModel.stockOut(qty, price, note)
                showStockOutDialog = false
            }
        )
    }
}

@Composable
private fun TransactionRow(transaction: StockTransaction) {
    val isIn = transaction.type == TransactionType.STOCK_IN
    val dateFormatted = remember(transaction.date) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar")).format(Date(transaction.date))
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                if (isIn) "إدخال" else "تخريج",
                fontWeight = FontWeight.SemiBold,
                color = if (isIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(dateFormatted, style = MaterialTheme.typography.bodyMedium)
            if (transaction.note.isNotBlank()) {
                Text(transaction.note, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("%.2f".format(transaction.quantity))
            Text(
                "بتكلفة %.2f".format(transaction.unitCost),
                style = MaterialTheme.typography.bodyMedium
            )
            transaction.profit?.let { profit ->
                Text(
                    "ربح: %.2f".format(profit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (profit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StockInDialog(
    unit: String,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, unitCost: Double, note: String) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var unitCost by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إدخال كمية") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("الكمية ($unit)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = unitCost,
                    onValueChange = { unitCost = it },
                    label = { Text("سعر الشراء للوحدة") },
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
                val q = quantity.toDoubleOrNull() ?: 0.0
                val c = unitCost.toDoubleOrNull() ?: 0.0
                onConfirm(q, c, note)
            }) { Text("تأكيد") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun StockOutDialog(
    unit: String,
    maxQuantity: Double,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, sellingPricePerUnit: Double?, note: String) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تخريج كمية") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "المتوفر: %.2f %s".format(maxQuantity, unit),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("الكمية ($unit)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("سعر البيع للوحدة (اختياري)") },
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
                val q = quantity.toDoubleOrNull() ?: 0.0
                val p = sellingPrice.toDoubleOrNull()
                onConfirm(q, p, note)
            }) { Text("تأكيد") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
