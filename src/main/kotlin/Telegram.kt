package org.example

fun main(args: Array<String>) {

    val botToken: String = args[0]
    val telegramBotService = TelegramBotService(botToken)

    var updates = ""
    var updateId = 0L
    var lastUpdateId = 0L
    var chatId = ""
    var messageText = ""
    var callbackData = ""

    val trainer = LearnWordsTrainer(3, 4)

    while (true) {
        Thread.sleep(2000)
        updates = telegramBotService.getUpdates(updateId)
        println(updates)

        lastUpdateId = getLastUpdateId(updates)
        if (lastUpdateId == -1L) continue else updateId = lastUpdateId + 1L

        chatId = getChatId(updates)

        messageText = getMessageText(updates).lowercase()
        if (messageText == "hello") telegramBotService.sendMessage(chatId, "Hello")

        if (messageText == "/start") telegramBotService.sendMenu(chatId)
    }
}

fun getLastUpdateId(updates: String): Long {
    val lastUpdateIdRegex = "\"update_id\":(\\d+)".toRegex()
    val matchResult = lastUpdateIdRegex.find(updates)
    return matchResult?.groups?.get(1)?.value?.toLongOrNull() ?: -1L
}

fun getMessageText(updates: String): String {
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val matchResult = messageTextRegex.find(updates)
    return matchResult?.groups?.get(1)?.value ?: ""
}

fun getChatId(updates: String): String {
    val chatIdRegex = "\"id\":(\\d+)".toRegex()
    val matchResult = chatIdRegex.find(updates)
    return matchResult?.groups?.get(1)?.value ?: ""
}

fun getCallbackData(updates: String): String {
    val dataRegex = "\"data\":\"(.+?)\"".toRegex()
    val matchResult = dataRegex.find(updates)
    return matchResult?.groups?.get(1)?.value ?: ""
}