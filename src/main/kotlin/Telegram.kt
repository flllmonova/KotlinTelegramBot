package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    val urlGetMe = "https://api.telegram.org/bot$botToken/getMe"
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates"

    val client: HttpClient = HttpClient.newBuilder().build()

    val requestOfBotInfo: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetMe)).build()
    val requestOfUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

    val responseOfBotInfo: HttpResponse<String> = client.send(requestOfBotInfo, HttpResponse.BodyHandlers.ofString())
    val responseOfUpdates: HttpResponse<String> = client.send(requestOfUpdates, HttpResponse.BodyHandlers.ofString())

    println(responseOfBotInfo.body())
    println(responseOfUpdates.body())
}