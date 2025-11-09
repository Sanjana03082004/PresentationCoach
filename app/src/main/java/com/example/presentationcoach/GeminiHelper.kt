//package com.example.presentationcoach
//
//import android.content.Context
//import android.net.Uri
//import android.util.Base64
//import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.RequestBody.Companion.toRequestBody
//import org.json.JSONObject
//import java.io.File
//import java.io.IOException
//
//class GeminiHelper(private val context: Context, private val apiKey: String) {
//    private val client = OkHttpClient()
//
//    fun analyzeVideo(uri: Uri, callback: (String) -> Unit) {
//        val file = getFileFromUri(uri) ?: run {
//            callback("Error: Unable to load video file.")
//            return
//        }
//
//        val encodedVideo = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP) // ✅ Encode video
//
//        // ✅ Request JSON with video & prompt
//        val jsonBody = JSONObject().apply {
//            put("contents", listOf(JSONObject().apply {
//                put("role", "user")
//                put("parts", listOf(
//                    JSONObject().apply {
//                        put("text", "Analyze this presentation video and provide feedback on tone, pace, body language, and suggestions for improvement in bullet points.")
//                    },
//                    JSONObject().apply {
//                        put("inlineData", JSONObject().apply {
//                            put("mimeType", "video/mp4")  // ✅ Specify video format
//                            put("data", encodedVideo)     // ✅ Send base64-encoded video
//                        })
//                    }
//                ))
//            }))
//        }
//
//        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
//
//        val request = Request.Builder()
//            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey") // ✅ Correct API endpoint
//            .post(requestBody)
//            .addHeader("Content-Type", "application/json")
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                callback("Error: Request failed - ${e.localizedMessage}")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    val responseBody = it.body?.string() ?: "No response body"
//                    if (!it.isSuccessful) {
//                        callback("Error: Unsuccessful response - ${it.code} ${it.message} \n Response: $responseBody")
//                        return
//                    }
//                    callback(parseResponse(responseBody))
//                }
//            }
//        })
//    }
//
//    private fun getFileFromUri(uri: Uri): File? {
//        return try {
//            context.contentResolver.openInputStream(uri)?.use { inputStream ->
//                val tempFile = File.createTempFile("video", ".mp4", context.cacheDir)
//                tempFile.outputStream().use { outputStream ->
//                    inputStream.copyTo(outputStream)
//                }
//                tempFile
//            }
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    private fun parseResponse(json: String?): String {
//        return try {
//            val jsonObject = JSONObject(json ?: "{}")
//            jsonObject.getJSONArray("candidates")
//                .getJSONObject(0)
//                .getJSONObject("content")
//                .getJSONArray("parts")
//                .getJSONObject(0)
//                .getString("text")
//        } catch (e: Exception) {
//            "Error: Failed to parse response."
//        }
//    }
//}
//
//package com.example.presentationcoach
//
//import android.content.Context
//import android.net.Uri
//import android.util.Base64
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.RequestBody.Companion.toRequestBody
//import org.json.JSONObject
//import java.io.File
//import com.iceteck.silicompressorr.SiliCompressor
//
//import java.io.IOException
//
//class GeminiHelper(private val context: Context, private val apiKey: String) {
//    private val client = OkHttpClient()
//
//    suspend fun analyzeVideo(
//        uri: Uri,
//        onSuccess: (String) -> Unit,
//        onError: (String) -> Unit
//    ) = withContext(Dispatchers.IO) {
//        try {
//            val compressedFile = compressVideo(uri) ?: throw Exception("Video compression failed")
//            val jsonBody = createRequestBody(compressedFile)
//
//            val request = Request.Builder()
//                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
//                .post(jsonBody)
//                .build()
//
//            val response = client.newCall(request).execute()
//            handleResponse(response, onSuccess, onError)
//
//        } catch (e: Exception) {
//            onError(e.message ?: "Unknown error")
//        }
//    }
//
//    private suspend fun compressVideo(uri: Uri): File? = withContext(Dispatchers.IO) {
//        try {
//            val originalFile = getFileFromUri(uri) ?: return@withContext null
//            val compressedFilePath = SiliCompressor.with(context).compressVideo(
//                originalFile.absolutePath,
//                context.cacheDir.absolutePath,
//                480,
//                640,
//                500_000
//            )
//            File(compressedFilePath)  // Convert the compressed file path (String) to a File object
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//
//    private fun createRequestBody(file: File): RequestBody {
//        val encodedVideo = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
//
//        val json = JSONObject().apply {
//            put("contents", JSONObject().apply {
//                put("parts", listOf(
//                    JSONObject().apply {
//                        put("text", """
//                            Analyze this presentation video and provide structured feedback on:
//                            1. Tone/Pitch/Volume
//                            2. Body Language
//                            3. Language/Vocabulary
//                            4. Engagement Suggestions
//                            Include timestamps where applicable.
//                        """.trimIndent())
//                    },
//                    JSONObject().apply {
//                        put("inlineData", JSONObject().apply {
//                            put("mimeType", "video/mp4")
//                            put("data", encodedVideo)
//                        })
//                    }
//                ))
//            })
//        }.toString()
//
//        return json.toRequestBody("application/json".toMediaType())
//    }
//
//    private fun handleResponse(
//        response: Response,
//        onSuccess: (String) -> Unit,
//        onError: (String) -> Unit
//    ) {
//        try {
//            if (!response.isSuccessful) {
//                onError("API Error: ${response.code} ${response.message}")
//                return
//            }
//
//            val responseBody = response.body?.string() ?: run {
//                onError("Empty response")
//                return
//            }
//
//            val result = JSONObject(responseBody)
//                .getJSONArray("candidates")
//                .getJSONObject(0)
//                .getJSONObject("content")
//                .getJSONArray("parts")
//                .getJSONObject(0)
//                .getString("text")
//
//            onSuccess(result)
//
//        } catch (e: Exception) {
//            onError("Parsing error: ${e.message}")
//        }
//    }
//
//    private fun getFileFromUri(uri: Uri): File? {
//        return try {
//            context.contentResolver.openInputStream(uri)?.use { input ->
//                File.createTempFile("video", ".mp4", context.cacheDir).apply {
//                    outputStream().use { output ->
//                        input.copyTo(output)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            null
//        }
//    }
//}
package com.example.presentationcoach

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import okio.Buffer
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class GeminiHelper(private val context: Context, private val apiKey: String) {
    private val client = OkHttpClient()

    suspend fun analyzeVideo(
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // Bypass compression for now—use the original file directly.
            val file = getFileFromUri(uri) ?: throw Exception("Could not get file from Uri")

            // Optional: Log file size for debugging
            println("File size: ${file.length()} bytes")
            val jsonBody = createRequestBody(file)

            // Create the JSON request body using the file's Base64 data.
            val buffer = Buffer()
            jsonBody.writeTo(buffer)
            println("Request JSON: ${buffer.readUtf8()}")


            // Build and execute the POST request.
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody)
                .build()

            val response = client.newCall(request).execute()
            handleResponse(response, onSuccess, onError)
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }

    // Create the JSON request body.
    private fun createRequestBody(file: File): RequestBody {
        // Read the file and convert it to a Base64 string.
        val encodedVideo = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)

        // Build the text part for the prompt.
        val textPart = JSONObject().apply {
            put("text", "Analyze this presentation video and provide structured feedback on:\n" +
                    "1. Tone/Pitch/Volume\n" +
                    "2. Body Language\n" +
                    "3. Language/Vocabulary\n" +
                    "4. Engagement Suggestions\n" +
                    "Include timestamps where applicable.")
        }

        // Build the inline video part.
        val videoPart = JSONObject().apply {
            put("inlineData", JSONObject().apply {
                put("mimeType", "video/mp4")
                put("data", encodedVideo)
            })
        }

        // Combine the text and video parts into an array.
        val partsArray = JSONArray().apply {
            put(textPart)
            put(videoPart)
        }

        // The content object holding the parts.
        val contentObject = JSONObject().apply {
            put("parts", partsArray)
        }

        // The API expects "contents" as an array.
        val contentsArray = JSONArray().apply {
            put(contentObject)
        }

        // Build the final JSON.
        val mainJson = JSONObject().apply {
            put("contents", contentsArray)
        }

        return mainJson.toString().toRequestBody("application/json".toMediaType())
    }

    // Handle and parse the API response.
    private fun handleResponse(
        response: Response,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val responseBody = response.body?.string()
            if (!response.isSuccessful || responseBody == null) {
                onError("API Error: ${response.code} ${response.message} - Response: $responseBody")
                return
            }
            val jsonResponse = JSONObject(responseBody)
            if (!jsonResponse.has("candidates")) {
                onError("Unexpected response format: $responseBody")
                return
            }
            val result = jsonResponse
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
            onSuccess(result)
        } catch (e: Exception) {
            onError("Parsing error: ${e.message}")
        }
    }

    // Utility function to create a temporary file from the provided Uri.
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                File.createTempFile("video", ".mp4", context.cacheDir).apply {
                    outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

