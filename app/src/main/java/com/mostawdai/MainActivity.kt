package com.mostawdai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.mostawdai.core.navigation.AppNavGraph
import com.mostawdai.core.theme.MostawdaiTheme
import com.mostawdai.data.local.AppPreferences
import com.mostawdai.data.local.ThemeMode
import com.mostawdai.feature.lock.AppLockScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val biometricAvailable = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS

        setContent {
            val themeMode by appPreferences.themeMode.collectAsState()
            val lockEnabled by appPreferences.lockEnabled.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MostawdaiTheme(darkTheme = darkTheme) {
                var unlocked by remember { mutableStateOf(!(lockEnabled && biometricAvailable)) }
                if (unlocked) {
                    AppNavGraph()
                } else {
                    AppLockScreen(onUnlocked = { unlocked = true })
                }
            }
        }
    }
}
