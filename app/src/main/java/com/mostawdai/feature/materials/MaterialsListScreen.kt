package com.mostawdai.feature.materials

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostawdai.domain.model.Material

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsListScreen(
    onAddMaterialClick: () -> Unit,
    onMaterialClick: (Long) -> Unit,
    onBackupClick: () -> Unit,
    viewModel: MaterialsListViewModel = hiltViewModel()
) {
    val summary by viewModel.summary.collectAsState()
    var query by remember { mutableStateOf("") }

    val filteredMaterials = remember(summary.materials, query) {
        if (query.isBlank()) summary.materials
        else summary.materials.filter { it.name.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مستودعي") },
                actions = {
                    IconButton(onClick = onBackupClick) {
                        Icon(Icons.Default.Share, contentDescription = "تصدير ونسخ احتياطي")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMaterialClick) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مادة")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("قيمة المخزون الإجمالية", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "%.2f".format(summary.totalInventoryValue),
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (summary.lowStockCount > 0) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${summary.lowStockCount} مادة منخفضة المخزون",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("بحث عن مادة") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            if (filteredMaterials.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (summary.materials.isEmpty()) "لا توجد مواد بعد.\nاضغط + لإضافة أول مادة"
                        else "لا توجد نتائج مطابقة للبحث",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredMaterials, key = { it.id }) { material ->
                        MaterialRow(material, onClick = { onMaterialClick(material.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialRow(material: Material, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(material.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "%.2f %s".format(material.currentQuantity, material.unit),
                style = MaterialTheme.typography.bodyMedium,
                color = if (material.isLowStock) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "%.2f".format(material.totalValue),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "متوسط التكلفة: %.2f".format(material.averageCost),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
