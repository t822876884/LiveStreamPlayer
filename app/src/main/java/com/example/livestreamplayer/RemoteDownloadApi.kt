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
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RemoteDownloadApi(private val context: Context) {

    private val client = OkHttpClient()
    private val preferenceManager = PreferenceManager(context)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()) // Assuming ISO 8601 format

    private fun getBaseUrl(): String? {
        val fullUrl = preferenceManager.getRemoteDownloadUrl()
        return if (!fullUrl.isNullOrEmpty()) {
            // Extract base URL from the full URL (e.g., http://localhost:3180)
            // Assuming the full URL is always in the format "http://host:port/download"
            fullUrl.substringBeforeLast("/")
        } else {
            null
        }
    }

    suspend fun cancelDownload(taskId: String): ApiResponse {
        return withContext(Dispatchers.IO) {
            val baseUrl = getBaseUrl() ?: return@withContext ApiResponse(false, "远程下载地址未配置")
            val url = "$baseUrl/cancel"
            val json = JSONObject().apply {
                put("id", taskId)
            }
            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

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
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

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
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
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
                                val streamUrl = taskJson.optString("url")
                                val outputPath = taskJson.optString("filePath") // Assuming filePath is outputPath
                                val statusString = taskJson.optString("status")
                                val errorMessage = taskJson.optString("errorMessage")
                                val startTimeString = taskJson.optString("startTime")
                                val endTimeString = taskJson.optString("endTime")

                                val startTime = try {
                                    dateFormat.parse(startTimeString)
                                } catch (e: Exception) {
                                    Date()
                                }
                                val endTime = try {
                                    if (endTimeString.isNotEmpty()) dateFormat.parse(endTimeString) else null
                                } catch (e: Exception) {
                                    null
                                }

                                val downloadStatus = when (statusString) {
                                    "downloading" -> DownloadStatus.DOWNLOADING
                                    "completed" -> DownloadStatus.COMPLETED
                                    "failed" -> DownloadStatus.ERROR
                                    "cancelled" -> DownloadStatus.CANCELLED
                                    else -> DownloadStatus.DOWNLOADING
                                }
                                tasks.add(DownloadTask(id, channelTitle, streamUrl, outputPath, startTime, endTime, downloadStatus, errorMessage))
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

    private fun parseResponse(response: okhttp3.Response, responseBody: String?): com.example.livestreamplayer.ApiResponse {
        return if (response.isSuccessful && responseBody != null) {
            val jsonResponse = JSONObject(responseBody)
            val ok = jsonResponse.optBoolean("ok", false)
            val message = jsonResponse.optString("message", "未知错误")
            com.example.livestreamplayer.ApiResponse(ok, message)
        } else {
            com.example.livestreamplayer.ApiResponse(false, responseBody ?: "请求失败")
        }
    }
}
