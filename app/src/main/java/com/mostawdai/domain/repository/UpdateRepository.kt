package com.mostawdai.domain.repository

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String
)

interface UpdateRepository {
    suspend fun checkForUpdate(): UpdateInfo?
}
