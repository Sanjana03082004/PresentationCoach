
import android.content.Context
import android.net.Uri
import android.service.voice.VoiceInteractionSession

import org.json.JSONObject
import java.io.File

class GeminiHelper(private val context: Context) {
    private val client = OkHttpClient()
    private val apiKey = "AIzaSyCtN4uHy3qjoKSfO6DyR2R7f4q_hUBPsTQ" // Get from https://aistudio.google.com/

    fun analyzeVideo(uri: Uri, callback: (String) -> Unit) {
        val file = getFileFromUri(uri) ?: run {
            callback("Error loading video")
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "parts",
                """
                {
                    "text": "Analyze this presentation video and provide feedback on tone, pace, body language, and suggestions for improvement in clear bullet points.",
                    "video": {
                        "mime_type": "video/mp4",
                        "data": "${file.readBytes().encodeToString()}"
                    }
                }
                """
            )
            .build()

        val request = VoiceInteractionSession.Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent")
            .post(requestBody)
            .addHeader("x-goog-api-key", apiKey)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val result = parseResponse(response.body?.string())
                callback(result)
            } catch (e: Exception) {
                callback("Analysis failed: ${e.message}")
            }
        }.start()
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("video", ".mp4", context.cacheDir)
            inputStream?.copyTo(tempFile.outputStream())
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun parseResponse(json: String?): String {
        return try {
            JSONObject(json!!)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            "Failed to parse response"
        }
    }
}