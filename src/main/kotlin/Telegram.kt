package org.example

fun main(args: Array<String>) {

    val botToken: String = args[0]
    val telegramBotService = TelegramBotService(botToken)

    var updates: String
    var updateId = 0L
    var lastUpdateId: Long
    var chatId: String
    var messageText: String
    var callbackData: String
    var statistics: Statistics

    val trainer = LearnWordsTrainer(3, 4)

    while (true) {
        Thread.sleep(2000)
        updates = telegramBotService.getUpdates(updateId)
        println(updates)

        lastUpdateId = getLastUpdateId(updates)
        if (lastUpdateId == -1L) continue else updateId = lastUpdateId + 1L

        chatId = getChatId(updates)

        messageText = getMessageText(updates).lowercase()
        when (messageText) {
            "hello" -> telegramBotService.sendMessage(chatId, "Hello")
            "/start" -> telegramBotService.sendMenu(chatId)
        }

        callbackData = getCallbackData(updates)
        when (callbackData) {
            CALLBACK_DATA_LEARNED_WORDS -> checkNextQuestionAndSend(trainer, telegramBotService, chatId)
            CALLBACK_DATA_STATISTICS -> {
                statistics = trainer.getStatistics()
                telegramBotService.sendMessageAndBackToMenuButton(statistics.statisticsToString(), chatId)
            }
            CALLBACK_DATA_BACK_TO_MENU -> telegramBotService.sendMenu(chatId)
        }

        if (callbackData.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
            checkAnswer(callbackData, trainer, telegramBotService, chatId)
            checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        }
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

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: String,
) {
    val question = trainer.getNextQuestion()
    if (trainer.isDictionaryEmpty) {
        telegramBotService.sendMessageAndBackToMenuButton("Невозможно загрузить словарь", chatId)
        return
    }
    if (question == null) {
        telegramBotService.sendMessageAndBackToMenuButton("✅ Все слова в словаре выучены", chatId)
        return
    } else telegramBotService.sendQuestion(chatId, question)
}

fun checkAnswer(
    callbackData: String,
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: String)
{
    val answerIndex = callbackData.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
    val correctAnswer = trainer.getCurrentQuestion()?.correctAnswer
    correctAnswer?.let {
        val resultMessage = if (trainer.checkAnswer(answerIndex)) "✅ Правильно!"
        else "❌ Неправильно! ${correctAnswer.original} – это ${correctAnswer.translate}"
        telegramBotService.sendMessage(chatId, resultMessage)
    } ?: telegramBotService.sendMessage(chatId, "\uD83D\uDD5C Ответ на вопрос не загрузился")
}