package org.example

import java.net.StandardProtocolFamily
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

private const val TELEGRAM_API = "https://api.telegram.org/bot"

class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TELEGRAM_API$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Int, message: String) {
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8)
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMenu(chatId: Int) {
        val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
        val sendMenuBody = """
            {
              "chat_id": $chatId,
              "text": "Основное меню",
              "reply_markup": {
                "inline_keyboard": [
                  [
                    {
                      "text": "Изучить слова",
                      "callback_data": "learn_words_clicked"
                    }
                  ],
                  [
                    {
                      "text": "Статистика",
                      "callback_data": "statistics_clicked"
                    }
                  ]  
                ]
              }
            }
        """.trimIndent()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }
}