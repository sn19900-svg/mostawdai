package com.mostawdai.feature.profit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitScreen(
    onBack: () -> Unit,
    viewModel: ProfitViewModel = hiltViewModel()
) {
    val overview by viewModel.overview.collectAsState()
    val isProfit = overview.netProfit >= 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الأرباح") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (isProfit) "صافي ربح" else "صافي خسارة",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "%.2f".format(overview.netProfit),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isProfit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfitRow("إجمالي إيراد المبيعات", overview.totalSalesRevenue)
                    HorizontalDivider()
                    ProfitRow("إجمالي المبلغ المدفوع للمواد", overview.totalPaidForMaterials)
                }
            }

            Text(
                "الأرباح = إجمالي إيراد المبيعات − إجمالي المبلغ المدفوع لشراء المواد منذ البداية. القيمة السالبة تعني أن المصروف على الشراء تجاوز الإيراد حتى الآن.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfitRow(label: String, value: Double) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text("%.2f".format(value), style = MaterialTheme.typography.titleMedium)
    }
}
