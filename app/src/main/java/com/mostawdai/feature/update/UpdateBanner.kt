package com.mostawdai.feature.update

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostawdai.core.util.ApkInstaller
import com.mostawdai.core.util.UpdateNotifier

@Composable
fun UpdateBanner(viewModel: UpdateViewModel = hiltViewModel()) {
    val updateInfo by viewModel.updateInfo.collectAsState()
    val context = LocalContext.current
    var isDownloading by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(updateInfo) {
        updateInfo?.let { info ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            UpdateNotifier.notifyUpdateAvailable(context, info.versionName)
        }
    }

    updateInfo?.let { info ->
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("يتوفر تحديث جديد", style = MaterialTheme.typography.titleMedium)
                    Text("النسخة ${info.versionName}", style = MaterialTheme.typography.bodyMedium)
                }
                Button(
                    onClick = {
                        isDownloading = true
                        ApkInstaller.downloadAndInstall(context, info.apkUrl, info.versionName)
                    },
                    enabled = !isDownloading
                ) {
                    Text(if (isDownloading) "جاري التنزيل..." else "تحديث")
                }
            }
        }
    }
}
