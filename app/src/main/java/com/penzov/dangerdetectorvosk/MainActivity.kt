package com.penzov.dangerdetectorvosk

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
// SpeechService vs SpeechStreamService
// SpeechService — работает с микрофоном напрямую (AudioRecord), подходит для онлайн-прослушивания (твоя текущая реализация).
// SpeechStreamService — работает с потоком аудио (InputStream), полезен если, например, читаешь из файла или стримишь RTSP-поток.
import org.vosk.android.SpeechService
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : ComponentActivity(), RecognitionListener {

    private var speechService: SpeechService? = null
    private lateinit var resultTextView: TextView
    private lateinit var tts: TextToSpeech

    private val dangerWords = listOf(
        "бомба", "убить", "нож", "стрелять", "взорвать", "террор", "громко", "кричать", "помогите", "опасность"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем TTS (Text-To-Speech) — для озвучивания фраз
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("ru")
            }
        }

        resultTextView = TextView(this).apply {
            text = "⏳ Загрузка..."
            textSize = 20f
            setPadding(16, 32, 16, 32)
        }
        setContentView(resultTextView)

        // Проверяем разрешение на микрофон
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            initModelAndStartRecognition()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initModelAndStartRecognition()
            } else {
                resultTextView.text = "❌ Нет доступа к микрофону"
            }
        }

    private fun initModelAndStartRecognition() {
        try {
            // Копируем модель Vosk из assets в файловую систему
            val modelDir = StorageService.unpackAssets(this, "model")
            val model = Model(modelDir.absolutePath)

            val recognizer = Recognizer(model, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening(this)

            resultTextView.text = "🎤 Говорите, я слушаю..."

        } catch (e: IOException) {
            Log.e("Vosk", "Ошибка загрузки модели: ${e.message}")
            resultTextView.text = "Ошибка загрузки модели: ${e.message}"
        }
    }

    override fun onPartialResult(hypothesis: String?) {
        // Можно добавить отображение живого текста
    }

    override fun onResult(hypothesis: String?) {
        hypothesis?.let {
            val text = extractText(it)
            val dangerDetected = containsDanger(text)

            if (dangerDetected) {
                resultTextView.text = "🚨 Обнаружено:\n$text"
                speak(text) // озвучка
                TelegramNotifier.sendAlert(text) // уведомление
            } else {
                resultTextView.text = "✅ Распознано:\n$text"
            }
        }
    }

    override fun onFinalResult(hypothesis: String?) {
        // Аналогично onResult()
        onResult(hypothesis)
    }

    override fun onError(exception: Exception?) {
        resultTextView.text = "❌ Ошибка: ${exception?.message}"
        Log.e("Vosk", "Ошибка: ${exception?.message}")
    }

    override fun onTimeout() {
        resultTextView.text = "⏰ Таймаут. Попробуйте снова."
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService?.stop()
        tts.shutdown()
    }

    private fun extractText(json: String): String {
        return try {
            val obj = JSONObject(json)
            obj.optString("text", "Не удалось распознать текст")
        } catch (e: Exception) {
            "Не удалось распознать текст"
        }
    }

    private fun containsDanger(text: String): Boolean {
        val lowercase = text.lowercase()
        return dangerWords.any { word -> lowercase.contains(word) }
    }

    // 🔊 Озвучиваем текст
    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}


