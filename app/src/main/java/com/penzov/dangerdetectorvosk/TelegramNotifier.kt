package com.penzov.dangerdetectorvosk

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TelegramNotifier {

    // 🔐 Токен бота и ID получателя — ЗАМЕНИ при необходимости
    private const val botToken = "token bot"
    private const val chatId = "186902597" // <- здесь мой Telegram ID (временно)

    fun sendAlert(text: String) {
        val encodedText = URLEncoder.encode("🚨 Опасность: $text", "UTF-8")
        val urlStr = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedText"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val responseCode = conn.responseCode
                Log.i("Telegram", "Ответ от Telegram: $responseCode")
            } catch (e: Exception) {
                Log.e("Telegram", "Ошибка отправки: ${e.message}")
            }
        }
    }
}
