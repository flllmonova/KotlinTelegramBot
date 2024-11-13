package org.example

fun main(args: Array<String>) {

    val botToken: String = args[0]
    val telegramBotService = TelegramBotService(botToken)

    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        val lastUpdateId = telegramBotService.getLastUpdateId(updates)
        if (lastUpdateId == -1) continue else updateId = lastUpdateId + 1

        val chatId = telegramBotService.getChatId(updates)

        val messageText = telegramBotService.getMessageText(updates)
        if (messageText.equals("Hello", ignoreCase = true)) telegramBotService.sendMessage(chatId)
    }
}