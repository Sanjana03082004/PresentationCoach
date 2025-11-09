//
//package com.example.presentationcoach
//
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.example.presentationcoach.databinding.ActivityMainBinding
//
//class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding
//    private val REQUEST_VIDEO_PICK = 100
//
//    private val apiKey by lazy { BuildConfig.GEMINI_API_KEY } // 🔹 Securely load API key
//    private val geminiHelper by lazy { GeminiHelper(this, apiKey) }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.btnSelectVideo.setOnClickListener {
//            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
//                type = "video/*"
//            }
//            startActivityForResult(intent, REQUEST_VIDEO_PICK)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_VIDEO_PICK && resultCode == RESULT_OK) {
//            data?.data?.let { videoUri ->
//                binding.tvStatus.text = "Analyzing video..."
//                analyzeVideo(videoUri)
//            }
//        }
//    }
//
//    private fun analyzeVideo(uri: Uri) {
//        geminiHelper.analyzeVideo(uri) { result ->
//            runOnUiThread {
//                binding.tvResult.text = result
//                binding.tvStatus.text = "Analysis complete!"
//            }
//        }
//    }
//}
//
//
package com.example.presentationcoach

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.presentationcoach.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_VIDEO_PICK = 100
    // Ensure BuildConfig.GEMINI_API_KEY is set properly (for example in your gradle.properties)
    private val geminiHelper by lazy { GeminiHelper(this, BuildConfig.GEMINI_API_KEY) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "video/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, REQUEST_VIDEO_PICK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_PICK && resultCode == RESULT_OK) {
            data?.data?.let { videoUri ->
                lifecycleScope.launch {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.tvStatus.text = "Analyzing..."
                    analyzeVideo(videoUri)
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }

    private suspend fun analyzeVideo(uri: Uri) {
        geminiHelper.analyzeVideo(uri,
            onSuccess = { result ->
                runOnUiThread {
                    binding.tvResult.text = result
                    binding.tvStatus.text = "Analysis complete!"
                }
            },
            onError = { error ->
                runOnUiThread {
                    binding.tvStatus.text = "Error: $error"
                }
            }
        )
    }
}
