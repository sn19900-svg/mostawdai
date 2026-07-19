package com.mostawdai

import android.app.Application
import com.mostawdai.data.local.SyncBackfill
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MostawdaiApp : Application() {

    @Inject lateinit var syncBackfill: SyncBackfill

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            syncBackfill.run()
        }
    }
}
