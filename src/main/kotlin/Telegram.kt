package org.example

fun main(args: Array<String>) {

    val botToken: String = args[0]
    val telegramBotService = TelegramBotService(botToken)

    var updates = ""
    var updateId = 0
    var lastUpdateId = 0
    var chatId = 0
    var messageText = ""
    var callbackData = ""

    val trainer = LearnWordsTrainer(3, 4)

    while (true) {
        Thread.sleep(2000)
        updates = telegramBotService.getUpdates(updateId)
        println(updates)

        lastUpdateId = getLastUpdateId(updates)
        if (lastUpdateId == -1) continue else updateId = lastUpdateId + 1

        chatId = getChatId(updates)

        messageText = getMessageText(updates).lowercase()
        if (messageText == "hello") telegramBotService.sendMessage(chatId, "Hello")

        if (messageText == "/start") telegramBotService.sendMenu(chatId)
    }
}

fun getLastUpdateId(updates: String): Int {
    val lastUpdateIdRegex = "\"update_id\":(\\d+)".toRegex()
    val matchResult = lastUpdateIdRegex.find(updates)
    return matchResult?.groups?.get(1)?.value?.toIntOrNull() ?: -1
}

fun getMessageText(updates: String): String {
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val matchResult = messageTextRegex.find(updates)
    return matchResult?.groups?.get(1)?.value ?: ""
}

fun getChatId(updates: String): Int {
    val chatIdRegex = "\"id\":(\\d+)".toRegex()
    val matchResult = chatIdRegex.find(updates)
    return matchResult?.groups?.get(1)?.value?.toInt() ?: -1
}

fun getCallbackData(updates: String): String {
    val dataRegex = "\"data\":\"(.+?)\"".toRegex()
    val matchResult = dataRegex.find(updates)
    return matchResult?.groups?.get(1)?.value ?: ""
}