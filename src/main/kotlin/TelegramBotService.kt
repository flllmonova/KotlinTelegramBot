package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

private const val TELEGRAM_API = "https://api.telegram.org/bot"
const val CALLBACK_DATA_LEARNED_WORDS = "learn_words_clicked"
const val CALLBACK_DATA_STATISTICS = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$TELEGRAM_API$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: String, message: String) {
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8)
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMenu(chatId: String) {
        val sendMessage = "$TELEGRAM_API$botToken/sendMessage"
        val sendMenuBody = """
            {
              "chat_id": $chatId,
              "text": "Основное меню",
              "reply_markup": {
                "inline_keyboard": [
                  [
                    {
                      "text": "Изучить слова",
                      "callback_data": "$CALLBACK_DATA_LEARNED_WORDS"
                    }
                  ],
                  [
                    {
                      "text": "Статистика",
                      "callback_data": "$CALLBACK_DATA_STATISTICS"
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

    fun sendQuestion(chatId: String, question: Question) {
        val sendMessage = "$TELEGRAM_API$botToken/sendMessage"
        val questionToJSON = question.variants
            .mapIndexed { index, word ->
                "[ { \"text\": \"${word.translate}\", " +
                        "\"callback_data\": \"${CALLBACK_DATA_ANSWER_PREFIX + index}\" } ]"
            }
            .joinToString(", ")
        val sendQuestionBody = """
            {
              "chat_id": $chatId,
              "text": "${question.correctAnswer.original}",
              "reply_markup": {
                "inline_keyboard": [
                  $questionToJSON
                ]
              }
            }
        """.trimIndent()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }
}