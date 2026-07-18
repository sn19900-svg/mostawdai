package com.mostawdai

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.mostawdai.core.navigation.AppNavGraph
import com.mostawdai.core.theme.MostawdaiTheme
import com.mostawdai.feature.lock.AppLockScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("mostawdai_prefs", Context.MODE_PRIVATE)
        val lockEnabled = prefs.getBoolean("app_lock_enabled", true)
        val biometricAvailable = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS

        val shouldLock = lockEnabled && biometricAvailable

        setContent {
            MostawdaiTheme {
                var unlocked by remember { mutableStateOf(!shouldLock) }
                if (unlocked) {
                    AppNavGraph()
                } else {
                    AppLockScreen(onUnlocked = { unlocked = true })
                }
            }
        }
    }
}
