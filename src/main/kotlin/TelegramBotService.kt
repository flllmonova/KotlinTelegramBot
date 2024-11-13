package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun getLastUpdateId(updates: String): Int {
        val lastUpdateIdRegex = "\"update_id\":(\\d+)".toRegex()
        val matchResult = lastUpdateIdRegex.find(updates)
        val groups = matchResult?.groups
        return groups?.get(1)?.value?.toIntOrNull() ?: -1
    }

    fun getMessageText(updates: String): String {
        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups: MatchGroupCollection? = matchResult?.groups
        val text: String = groups?.get(1)?.value ?: "no new messages"
        return text
    }

    fun getChatId(updates: String): Int {
        val chatIdRegex = "\"id\":(\\d+)".toRegex()
        val matchResult = chatIdRegex.find(updates)
        val chatId = matchResult?.groups?.get(1)?.value?.toInt() ?: -1
        return chatId
    }

    fun sendMessage(chatId: Int) {
        val text = "Hello"
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }
}

