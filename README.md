# 🎙️ PresentationCoach

> An AI-powered Android application that analyzes presentation videos and provides structured feedback on tone, language, body language, and engagement — powered by **Google Gemini 1.5 Flash**.

---

## 📱 Overview

**PresentationCoach** allows users to upload (or pick) a presentation video, sends it to the **Gemini multimodal API**, and receives detailed feedback to help improve public speaking skills.  

The app uses:
- `OkHttp` for network calls  
- `Coroutines` for asynchronous background processing  
- `Gemini 1.5 Flash` API for multimodal (video + text) analysis  
- ViewBinding for UI management

---

## 🚀 Features

✅ **Upload or select a video** from your device  
✅ **Analyze tone, pitch, and body language**  
✅ **Get feedback on vocabulary and engagement**  
✅ **Display AI-generated suggestions** with timestamps  
✅ Lightweight Kotlin + Android implementation  
✅ Secure API key handling through `gradle.properties`

---

## 🧠 How It Works

1. User selects a video file (`.mp4`) from the device.  
2. The app encodes it in Base64 and sends it to Gemini’s `generateContent` endpoint:  
https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=YOUR_API_KEY

csharp
Copy code
3. The model analyzes the content based on this structured prompt:
Analyze this presentation video and provide feedback on:

Tone/Pitch/Volume

Body Language

Language/Vocabulary

Engagement Suggestions
Include timestamps where applicable.

yaml
Copy code
4. The API returns a structured JSON response → parsed and displayed in the UI.

---

## 🧩 Project Structure

app/
├── src/
│ └── main/
│ ├── java/com/example/presentationcoach/
│ │ ├── GeminiHelper.kt # Handles Gemini API request/response
│ │ ├── MainActivity.kt # UI + video selection logic
│ │ └── ui/theme/ # App theme (Color, Theme, Type)
│ │
│ ├── res/ # Android resource files
│ │ ├── layout/activity_main.xml # UI layout with button, progress bar, result text
│ │ ├── values/ # Colors, strings, and styles
│ │ ├── drawable/ # Icons and vector assets
│ │ └── mipmap/ # App launcher icons
│ │
│ └── AndroidManifest.xml
│
├── build.gradle.kts # App-level Gradle config
├── settings.gradle.kts # Project name and Gradle setup
├── gradle.properties # Store GEMINI_API_KEY here (local only)
└── ...

yaml
Copy code

---

## 🔐 API Key Setup

1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey)  
2. Generate your **Gemini API Key**.  
3. Add it inside your `gradle.properties` (safe local file):
   ```properties
   GEMINI_API_KEY=YOUR_API_KEY_HERE
In build.gradle.kts (app level), add:

kotlin
Copy code
buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY")}\"")
This ensures your API key is not hardcoded in the codebase.

🧰 Dependencies
In build.gradle.kts:

kotlin
Copy code
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.json:json:20231013")
}
🎬 Usage
Run the app on an emulator or physical Android device.

Tap Select Video → pick a .mp4 file.

Wait while the progress bar shows “Analyzing…”

Once analysis completes, read AI feedback on-screen.

📸 UI Preview (Example)
Step	Screen
1️⃣ Select Video	
2️⃣ Analyzing	
3️⃣ Result	

⚠️ Notes
The Gemini API currently supports short clips (keep under ~30–60 MB for smooth results).

If upload fails, check your Logcat for “Request JSON” or “API Error” logs.

Ensure your device has Internet access; API requires live network.

🧑‍💻 Author
Sanjana Madpalwar
🎓 B.Tech in IT @ GNITS
💡 Passionate about AI, Android, and Human-Centered Computing
📧 sanjanamadpalwar@gmail.com

🪪 License
This project is licensed under the MIT License.
You’re free to use, modify, and distribute it with attribution.

🌟 Future Enhancements
🎤 Real-time feedback while recording

📈 Speech clarity & confidence score

🗣️ Emotion detection with facial cues

☁️ Cloud upload and history tracking

“Your presentation isn’t just about what you say — it’s how you make people feel.” 💬

yaml
