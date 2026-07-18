package com.mostawdai.feature.lock

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.mostawdai.core.util.BiometricAuthHelper

@Composable
fun AppLockScreen(onUnlocked: () -> Unit) {
    val activity = LocalContext.current as? FragmentActivity
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun tryAuthenticate() {
        errorMessage = null
        activity?.let {
            BiometricAuthHelper.authenticate(
                activity = it,
                onSuccess = onUnlocked,
                onFailure = { msg -> errorMessage = msg }
            )
        }
    }

    LaunchedEffect(Unit) { tryAuthenticate() }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("مستودعي مقفل", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            Button(onClick = { tryAuthenticate() }) { Text("فتح القفل") }
        }
    }
}
