package com.mostawdai.feature.materials

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostawdai.domain.model.Material

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsListScreen(
    onAddMaterialClick: () -> Unit,
    viewModel: MaterialsListViewModel = hiltViewModel()
) {
    val summary by viewModel.summary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("مستودعي") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMaterialClick) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مادة")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // ملخص سريع لقيمة المخزون
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

            if (summary.materials.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "لا توجد مواد بعد.\nاضغط + لإضافة أول مادة",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(summary.materials, key = { it.id }) { material ->
                        MaterialRow(material)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialRow(material: Material) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
