package com.mostawdai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mostawdai.core.navigation.AppNavGraph
import com.mostawdai.core.theme.MostawdaiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MostawdaiTheme {
                AppNavGraph()
            }
        }
    }
}
