package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

fun main(args: Array<String>) {

    val botToken: String = args[0]
    val telegramBotService = TelegramBotService(botToken)

    var responseString: String
    var response: Response
    var sortedUpdates: List<Update>
    var updateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(1500)
        responseString = telegramBotService.getUpdates(updateId)
        println(responseString)
        response = telegramBotService.json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, telegramBotService, trainers) }
        updateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    telegramBotService: TelegramBotService,
    trainers: HashMap<Long, LearnWordsTrainer>
) {
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val messageText = update.message?.text ?: ""
    val callbackData = update.callbackQuery?.data ?: ""
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    when (messageText) {
        "hello" -> telegramBotService.sendMessage(chatId, "Hello")
        "/start" -> telegramBotService.sendMenu(chatId)
    }

    when (callbackData) {
        CALLBACK_DATA_LEARNED_WORDS -> checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        CALLBACK_DATA_STATISTICS -> {
            val statistics = trainer.getStatistics()
            telegramBotService.sendMessageAndMenuBackButton(statistics.statisticsToString(), chatId)
        }

        CALLBACK_DATA_MENU_BACK -> telegramBotService.sendMenu(chatId)
        CALLBACK_DATA_RESET_RESULT_QUESTION -> telegramBotService.resetProgressQuestion(chatId, trainer)
        CALLBACK_DATA_RESET_RESULT -> {
            trainer.resetProgress()
            telegramBotService.sendMessageAndMenuBackButton("☑\uFE0F Ваш прогресс сброшен", chatId)
        }
    }

    if (callbackData.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
        checkAnswer(callbackData, trainer, telegramBotService, chatId)
        checkNextQuestionAndSend(trainer, telegramBotService, chatId)
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
) {
    val question = trainer.getNextQuestion()
    if (trainer.isDictionaryEmpty) {
        telegramBotService.sendMessageAndMenuBackButton("\uD83D\uDD5C Невозможно загрузить словарь", chatId)
        return
    }
    if (question == null) {
        telegramBotService.sendMessageAndMenuBackButton("✅ Все слова в словаре выучены", chatId)
        return
    } else telegramBotService.sendQuestion(chatId, question)
}

fun checkAnswer(
    callbackData: String,
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long
) {
    val userAnswerIndex = callbackData.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
    val correctAnswer = trainer.getCurrentQuestion()?.correctAnswer
    correctAnswer?.let {
        val resultMessage = if (trainer.checkAnswer(userAnswerIndex)) "✅ Правильно!"
        else "❌ Неправильно! ${correctAnswer.original} – это ${correctAnswer.translate}"
        telegramBotService.sendMessage(chatId, resultMessage)
    } ?: telegramBotService.sendMessage(chatId, "\uD83D\uDD5C Ответ на вопрос не загрузился")
}