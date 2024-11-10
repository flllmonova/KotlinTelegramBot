package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken: String = args[0]
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val lastUpdateId = getLastUpdateId(updates)
        if (lastUpdateId == null) continue else updateId = lastUpdateId + 1

        println(getMessageText(updates))
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

    return response.body()
}

fun getLastUpdateId(updates: String): Int? {
    val lastUpdateIdRegex = "\"update_id\":(\\d+)".toRegex()
    val matchResult = lastUpdateIdRegex.find(updates)
    val groups = matchResult?.groups
    return groups?.get(1)?.value?.toIntOrNull()
}

fun getMessageText(updates: String): String {
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val matchResult: MatchResult? = messageTextRegex.find(updates)
    val groups: MatchGroupCollection? = matchResult?.groups
    val text: String = groups?.get(1)?.value ?: "no new messages"
    return text
}