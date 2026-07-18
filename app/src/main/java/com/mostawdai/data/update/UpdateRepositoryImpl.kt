package com.mostawdai.data.update

import com.mostawdai.BuildConfig
import com.mostawdai.domain.repository.UpdateInfo
import com.mostawdai.domain.repository.UpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor() : UpdateRepository {

    override suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$OWNER/$REPO/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode != 200) return@withContext null

            val text = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)

            val tagName = json.optString("tag_name", "")
            val remoteVersionCode = tagName.removePrefix("v").toIntOrNull() ?: return@withContext null

            if (remoteVersionCode <= BuildConfig.VERSION_CODE) return@withContext null

            val assets = json.optJSONArray("assets") ?: return@withContext null
            var apkUrl: String? = null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.optString("name").endsWith(".apk")) {
                    apkUrl = asset.optString("browser_download_url")
                    break
                }
            }
            apkUrl ?: return@withContext null

            UpdateInfo(
                versionCode = remoteVersionCode,
                versionName = json.optString("name", tagName),
                apkUrl = apkUrl,
                releaseNotes = json.optString("body", "")
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val OWNER = "sn19900-svg"
        private const val REPO = "mostawdai"
    }
}
