package org.example

fun main(args: Array<String>) {

    val botToken: String = args[0]
    val telegramBotService = TelegramBotService(botToken)

    var updates = ""
    var updateId = 0
    var lastUpdateId = 0
    var chatId = 0
    var messageText = ""

    while (true) {
        Thread.sleep(2000)
        updates = telegramBotService.getUpdates(updateId)
        println(updates)

        lastUpdateId = getLastUpdateId(updates)
        if (lastUpdateId == -1) continue else updateId = lastUpdateId + 1

        chatId = getChatId(updates)

        messageText = getMessageText(updates)
        if (messageText.equals("Hello", ignoreCase = true)) telegramBotService.sendMessage(chatId)
    }
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