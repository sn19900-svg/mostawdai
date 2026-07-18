package com.mostawdai.feature.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostawdai.core.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showImportConfirm by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                viewModel.importFullBackup(stream)
            }
        }
    }

    LaunchedEffect(state.fileToShare) {
        state.fileToShare?.let { file ->
            ShareUtils.shareFile(
                context = context,
                file = file,
                mimeType = state.shareMimeType ?: "*/*",
                chooserTitle = "مشاركة الملف"
            )
            viewModel.consumeShareFile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التصدير والنسخ الاحتياطي") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("تقارير", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = viewModel::exportExcel,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isBusy
            ) { Text("تصدير المخزون والحركات إلى Excel") }

            Button(
                onClick = viewModel::exportPdf,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isBusy
            ) { Text("تصدير تقرير المخزون إلى PDF") }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text("نقل البيانات بين الأجهزة", style = MaterialTheme.typography.titleMedium)
            Text(
                "يُصدّر نسخة كاملة من كل المواد والحركات كملف واحد يمكن مشاركته وفتحه على جهاز آخر فيه نفس التطبيق.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = viewModel::exportFullBackup,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isBusy
            ) { Text("مشاركة نسخة كاملة (لجهاز آخر)") }

            OutlinedButton(
                onClick = { showImportConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isBusy
            ) { Text("استيراد نسخة من جهاز آخر") }

            if (state.isBusy) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.message?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(msg, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text("تحذير") },
            text = {
                Text("سيتم استبدال جميع البيانات الحالية على هذا الجهاز بمحتوى الملف المستورد. هل أنت متأكد؟")
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    importLauncher.launch("application/json")
                }) { Text("متابعة") }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) { Text("إلغاء") }
            }
        )
    }
}
