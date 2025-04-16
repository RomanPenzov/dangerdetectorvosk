// Файл: MainActivity.kt
// Это главный файл активности приложения DangerDetectorVosk. Здесь реализуется оффлайн-распознавание русской речи с помощью библиотеки Vosk
// и анализ текста на наличие опасных слов. Используем TextView для вывода результатов в реальном времени.

package com.penzov.dangerdetectorvosk

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity(), RecognitionListener {

    // Объект для работы с распознаванием речи
    private var speechService: SpeechService? = null

    // Текстовое поле для отображения распознанного текста
    private lateinit var resultTextView: TextView

    // Список слов, считающихся опасными — можно расширить
    private val dangerWords = listOf(
        "бомба", "убить", "нож", "стрелять", "взорвать", "террор", "громко", "кричать", "помогите", "опасность"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем текстовое поле и задаем его параметры
        resultTextView = TextView(this).apply {
            text = "Ожидание речи..."
            textSize = 20f
            setPadding(16, 32, 16, 32)
        }
        setContentView(resultTextView)

        // Проверяем наличие разрешения на запись с микрофона
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Если нет — запрашиваем у пользователя
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            // Если есть — сразу запускаем модель и распознавание
            initModelAndStartRecognition()
        }
    }

    // Объект для обработки результата запроса разрешения
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initModelAndStartRecognition()
            } else {
                resultTextView.text = "❌ Нет доступа к микрофону"
            }
        }

    // Метод для загрузки модели из assets и запуска распознавания речи
    private fun initModelAndStartRecognition() {
        try {
            // Копируем модель из assets в файловую систему
            val modelDir = unpackAssets("model")

            // Загружаем модель из скопированной папки
            val model = Model(modelDir.absolutePath)

            // Создаем распознаватель с нужной частотой
            val recognizer = Recognizer(model, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)

            // Начинаем слушать микрофон
            speechService?.startListening(this)

            resultTextView.text = "🎤 Говорите, я слушаю..."

        } catch (e: IOException) {
            e.printStackTrace()
            resultTextView.text = "Ошибка загрузки модели: ${e.message}"
        }
    }

    // Метод вызывается, когда есть промежуточный результат (не финальный)
    override fun onPartialResult(hypothesis: String?) {
        // Можно реализовать отображение "живого" текста (по желанию)
    }

    // Метод вызывается при получении результата распознавания речи
    override fun onResult(hypothesis: String?) {
        hypothesis?.let {
            val text = extractText(it)
            val dangerDetected = containsDanger(text)

            // Обновляем интерфейс в зависимости от наличия опасных слов
            resultTextView.text = if (dangerDetected) {
                "🚨 Обнаружено:\n$text"
            } else {
                "✅ Распознано:\n$text"
            }
        }
    }

    // Метод вызывается при финальном результате (когда пауза в речи)
    override fun onFinalResult(hypothesis: String?) {
        hypothesis?.let {
            val text = extractText(it)
            val dangerDetected = containsDanger(text)

            resultTextView.text = if (dangerDetected) {
                "🛑 Опасность!\n$text"
            } else {
                "🔚 Финальный результат:\n$text"
            }
        }
    }

    // Обработка ошибок при распознавании
    override fun onError(exception: Exception?) {
        resultTextView.text = "❌ Ошибка: ${exception?.message}"
    }

    // Вызывается, если долгое время не было речи
    override fun onTimeout() {
        resultTextView.text = "⏰ Таймаут. Попробуйте снова."
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService?.stop()
        speechService = null
    }

    // Вспомогательная функция — извлекает текст из JSON-строки, которую возвращает Vosk
    private fun extractText(json: String): String {
        return Regex("\"text\"\\s*:\\s*\"(.*?)\"")
            .find(json)
            ?.groups?.get(1)
            ?.value ?: "Не удалось распознать текст"
    }

    // Проверка: содержит ли строка хотя бы одно из опасных слов
    private fun containsDanger(text: String): Boolean {
        val lowercase = text.lowercase()
        return dangerWords.any { word -> lowercase.contains(word) }
    }

    // Копируем модель из assets в директорию filesDir, если она ещё не была скопирована
    private fun unpackAssets(assetPath: String): File {
        val assetManager = assets
        val outDir = File(filesDir, assetPath)
        if (outDir.exists()) return outDir

        val files = assetManager.list(assetPath) ?: return outDir
        outDir.mkdirs()

        for (filename in files) {
            val inPath = "$assetPath/$filename"
            val outFile = File(outDir, filename)
            val subFiles = assetManager.list(inPath)

            if (!subFiles.isNullOrEmpty()) {
                unpackAssets(inPath)
            } else {
                assetManager.open(inPath).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }

        return outDir
    }
}

