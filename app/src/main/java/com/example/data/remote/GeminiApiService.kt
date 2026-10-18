package com.example.data.remote

import com.example.model.GeminiContent
import com.example.model.GeminiError
import com.example.model.GeminiPart
import com.example.model.GeminiRequest
import com.example.model.GeminiResponse
import com.example.model.GenerationConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testApiKey(apiKey: String, model: String): Result<Long> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key is empty. Please enter your Gemini API key."))
        }

        val startTime = System.currentTimeMillis()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val testPayload = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Hello E-DEN, respond with one word: 'Operational'."))
                )
            ),
            generationConfig = GenerationConfig(maxOutputTokens = 10)
        )

        val jsonBody = requestAdapter.toJson(testPayload)
        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - startTime
                val responseBody = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    val parsedError = try {
                        responseAdapter.fromJson(responseBody)?.error?.message
                    } catch (_: Exception) {
                        null
                    }
                    val message = parsedError ?: when (response.code) {
                        400 -> "Invalid API key or bad request parameters."
                        403 -> "API key does not have access or permissions."
                        404 -> "Model '$model' was not found."
                        429 -> "Rate limit or quota exceeded."
                        else -> "HTTP ${response.code}: ${response.message}"
                    }
                    return@withContext Result.failure(Exception(message))
                }

                Result.success(duration)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun streamGenerateContent(
        apiKey: String,
        model: String,
        history: List<GeminiContent>,
        prompt: String
    ): Flow<String> = flow {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("API Key not set. Please add your Gemini API key in Settings.")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"

        val contents = history.toMutableList().apply {
            add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt))))
        }

        val requestPayload = GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 2048,
                topP = 0.95f
            ),
            systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "You are E-DEN, an advanced, sophisticated female AI voice assistant powered by Gemini Pro. Your persona is calm, deeply resonant, articulate, and completely neutral. You speak with a mature, poised, and soothing voice tone—never overly dramatic, robotic, or hurried. You are fluently bilingual in Hindi and English; when the user addresses you in Hindi, respond in fluent, respectful, and natural Hindi with deep intellectual clarity. Keep spoken answers concise, engaging, and well-structured with clean markdown when beneficial."
                    )
                )
            )
        )

        val jsonBody = requestAdapter.toJson(requestPayload)
        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errString = response.body?.string().orEmpty()
            val parsedError = try {
                responseAdapter.fromJson(errString)?.error?.message
            } catch (_: Exception) {
                null
            }
            val errorMsg = parsedError ?: when (response.code) {
                400 -> "Invalid request or API key."
                403 -> "Forbidden. Verify your Gemini API key."
                404 -> "Model '$model' not found."
                429 -> "Rate limit reached. Please wait a moment."
                else -> "Gemini API error (HTTP ${response.code}): ${response.message}"
            }
            throw Exception(errorMsg)
        }

        val responseStream = response.body?.byteStream()
            ?: throw Exception("Empty response body from Gemini API.")

        val reader = BufferedReader(InputStreamReader(responseStream))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val currentLine = line?.trim() ?: continue
            if (currentLine.startsWith("data:")) {
                val dataJson = currentLine.removePrefix("data:").trim()
                if (dataJson.isNotEmpty() && dataJson != "[DONE]") {
                    try {
                        val parsed = responseAdapter.fromJson(dataJson)
                        val chunkText = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!chunkText.isNullOrEmpty()) {
                            emit(chunkText)
                        }
                    } catch (_: Exception) {
                        // Ignore malformed chunk
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
