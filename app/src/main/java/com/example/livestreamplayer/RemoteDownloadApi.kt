package com.example.livestreamplayer

import android.content.Context
import android.util.Log
import com.example.livestreamplayer.TasksResponse
import com.example.livestreamplayer.DownloadTask
import com.example.livestreamplayer.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.net.URI

class RemoteDownloadApi(private val context: Context) {

    private val client = OkHttpClient()
    private val preferenceManager = PreferenceManager(context)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()) // Assuming ISO 8601 format

    private fun getBaseUrl(): String? {
        val fullUrl = preferenceManager.getRemoteDownloadUrl()
        if (fullUrl.isNullOrEmpty()) return null
        return try {
            val uri = URI(fullUrl)
            val scheme = uri.scheme ?: "http"
            val host = uri.host ?: return fullUrl
            val port = if (uri.port != -1) ":" + uri.port else ""
            scheme + "://" + host + port
        } catch (e: Exception) {
            fullUrl
        }
    }

    suspend fun cancelDownload(taskId: String?, title: String?): ApiResponse {
        return withContext(Dispatchers.IO) {
            val baseUrl = getBaseUrl() ?: return@withContext ApiResponse(false, "远程下载地址未配置")
            val url = "$baseUrl/cancel"
            val json = JSONObject().apply {
                if (!taskId.isNullOrBlank()) put("id", taskId) else if (!title.isNullOrBlank()) put("title", title)
            }
            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val builder = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
            PreferenceManager(context).getRemoteAuthToken()?.let { token ->
                if (token.isNotBlank()) builder.addHeader("Authorization", "Bearer $token")
            }
            val request = builder.build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    return@withContext parseResponse(response, responseBody)
                }
            } catch (e: IOException) {
                Log.e("RemoteDownloadApi", "Cancel download request failed", e)
                return@withContext ApiResponse(false, "网络请求失败: ${e.message}")
            }
        }
    }

    suspend fun deleteCompletedFile(taskId: String): ApiResponse {
        return withContext(Dispatchers.IO) {
            val baseUrl = getBaseUrl() ?: return@withContext ApiResponse(false, "远程下载地址未配置")
            val url = "$baseUrl/delete"
            val json = JSONObject().apply {
                put("id", taskId)
            }
            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val builder = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
            PreferenceManager(context).getRemoteAuthToken()?.let { token ->
                if (token.isNotBlank()) builder.addHeader("Authorization", "Bearer $token")
            }
            val request = builder.build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    return@withContext parseResponse(response, responseBody)
                }
            } catch (e: IOException) {
                Log.e("RemoteDownloadApi", "Delete completed file request failed", e)
                return@withContext ApiResponse(false, "网络请求失败: ${e.message}")
            }
        }
    }

    suspend fun getTasks(status: String, page: Int, pageSize: Int): TasksResponse {
        return withContext(Dispatchers.IO) {
            val baseUrl = getBaseUrl() ?: return@withContext TasksResponse(false, emptyList(), 0, 0, 0, "远程下载地址未配置")
            val url = "$baseUrl/tasks?status=$status&page=$page&pageSize=$pageSize"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        val contentType = response.header("Content-Type") ?: ""
                        if (!contentType.contains("application/json")) {
                            return@withContext TasksResponse(false, emptyList(), 0, 0, 0, "响应类型错误: $contentType")
                        }
                        val jsonResponse = try {
                            JSONObject(responseBody)
                        } catch (e: JSONException) {
                            return@withContext TasksResponse(false, emptyList(), 0, 0, 0, "解析错误: ${e.message}")
                        }
                        val ok = jsonResponse.optBoolean("ok", false)
                        val message = jsonResponse.optString("message", "未知错误")
                        val itemsArray = jsonResponse.optJSONArray("items")
                        val total = jsonResponse.optInt("total", 0)
                        val currentPage = jsonResponse.optInt("page", page)
                        val currentPagesize = jsonResponse.optInt("pageSize", pageSize)

                        val tasks = mutableListOf<DownloadTask>()
                        itemsArray?.let {
                            for (i in 0 until it.length()) {
                                val taskJson = it.getJSONObject(i)

                                val id = taskJson.optString("id")
                                val channelTitle = taskJson.optString("title")

                                // url 字段可能带有反引号或空格，做清洗
                                val rawUrl = optStringMulti(taskJson, "url")
                                val streamUrl = rawUrl.replace("`", "").trim()

                                // 兼容 file_path 与 filePath
                                val outputPath = optStringMulti(taskJson, "file_path", "filePath")

                                // 兼容 created_at/startTime 与 updated_at/endTime
                                val startTimeString = optStringMulti(taskJson, "created_at", "startTime")
                                val endTimeString = optStringMulti(taskJson, "updated_at", "endTime")

                                val startTime = try {
                                    if (startTimeString.isNotEmpty()) dateFormat.parse(startTimeString) else Date()
                                } catch (e: Exception) {
                                    Date()
                                }
                                val endTime = try {
                                    if (endTimeString.isNotEmpty()) dateFormat.parse(endTimeString) else null
                                } catch (e: Exception) {
                                    null
                                }

                                val statusString = taskJson.optString("status")
                                val errorMessage = optStringMulti(taskJson, "errorMessage", "error")

                                val downloadStatus = when (statusString) {
                                    "downloading" -> DownloadStatus.DOWNLOADING
                                    "completed" -> DownloadStatus.COMPLETED
                                    "failed" -> DownloadStatus.ERROR
                                    "cancelled" -> DownloadStatus.CANCELLED
                                    else -> DownloadStatus.DOWNLOADING
                                }
                                tasks.add(
                                    DownloadTask(
                                        id = id,
                                        channelTitle = channelTitle,
                                        streamUrl = streamUrl,
                                        outputPath = outputPath,
                                        startTime = startTime,
                                        endTime = endTime,
                                        status = downloadStatus,
                                        errorMessage = errorMessage
                                    )
                                )
                            }
                        }
                        return@withContext TasksResponse(ok, tasks, total, currentPage, currentPagesize, message)
                    } else {
                        val message = response.body?.string() ?: "请求失败"
                        return@withContext TasksResponse(false, emptyList(), 0, 0, 0, message)
                    }
                }
            } catch (e: IOException) {
                Log.e("RemoteDownloadApi", "Get tasks request failed", e)
                return@withContext TasksResponse(false, emptyList(), 0, 0, 0, "网络请求失败: ${e.message}")
            }
        }
    }

    fun getCompletedFileStreamUrl(taskId: String): String? {
        val baseUrl = getBaseUrl() ?: return null
        return "$baseUrl/file?id=$taskId"
    }

    private fun optStringMulti(json: JSONObject, vararg keys: String): String {
        for (k in keys) {
            val v = json.optString(k, "")
            if (!v.isNullOrEmpty()) return v
        }
        return ""
    }

    private fun parseResponse(response: okhttp3.Response, responseBody: String?): com.example.livestreamplayer.ApiResponse {
        if (!response.isSuccessful || responseBody == null) {
            return com.example.livestreamplayer.ApiResponse(false, responseBody ?: "请求失败")
        }
        val contentType = response.header("Content-Type") ?: ""
        if (!contentType.contains("application/json")) {
            return com.example.livestreamplayer.ApiResponse(false, "响应类型错误: $contentType")
        }
        return try {
            val jsonResponse = JSONObject(responseBody)
            val ok = jsonResponse.optBoolean("ok", false)
            val message = jsonResponse.optString("message", "未知错误")
            val taskId = jsonResponse.optString("taskId", null)
            val id = jsonResponse.optString("id", null)
            com.example.livestreamplayer.ApiResponse(ok, message, taskId = if (taskId.isNullOrEmpty()) null else taskId, id = if (id.isNullOrEmpty()) null else id)
        } catch (e: JSONException) {
            com.example.livestreamplayer.ApiResponse(false, "解析错误: ${e.message}")
        }
    }
}
