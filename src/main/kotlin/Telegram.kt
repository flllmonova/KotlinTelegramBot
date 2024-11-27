package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

import CALLBACK_DATA_ANSWER_PREFIX
import CALLBACK_DATA_MENU_BACK
import CALLBACK_DATA_LEARNED_WORDS
import CALLBACK_DATA_RESET_RESULT
import CALLBACK_DATA_RESET_RESULT_QUESTION
import CALLBACK_DATA_STATISTICS
import LearnWordsTrainer
import Statistics
import TelegramBotService
import kotlinx.serialization.SerialName
import statisticsToString

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

    val json = Json { ignoreUnknownKeys = true }

    val botToken: String = args[0]
    val telegramBotService = TelegramBotService(botToken)

    var updates: List<Update>
    var responseString: String
    var response: Response
    var updateId = 0L
    var firstUpdate: Update?
    var chatId: Long?
    var messageText: String
    var callbackData: String
    var statistics: Statistics

    val trainer = LearnWordsTrainer(3, 4)

    while (true) {
        Thread.sleep(1500)
        responseString = telegramBotService.getUpdates(updateId)
        println(responseString)
        response = json.decodeFromString(responseString)
        updates = response.result
        firstUpdate = updates.firstOrNull() ?: continue
        updateId = firstUpdate.updateId + 1

        chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id ?: 0L

        messageText = firstUpdate.message?.text ?: ""
        when (messageText) {
            "hello" -> telegramBotService.sendMessage(chatId, "Hello")
            "/start" -> telegramBotService.sendMenu(json, chatId)
        }

        callbackData = firstUpdate.callbackQuery?.data ?: ""
        when (callbackData) {
            CALLBACK_DATA_LEARNED_WORDS -> checkNextQuestionAndSend(json, trainer, telegramBotService, chatId)
            CALLBACK_DATA_STATISTICS -> {
                statistics = trainer.getStatistics()
                telegramBotService.sendMessageAndMenuBackButton(json, statistics.statisticsToString(), chatId)
            }
            CALLBACK_DATA_MENU_BACK -> telegramBotService.sendMenu(json, chatId)
            CALLBACK_DATA_RESET_RESULT_QUESTION -> telegramBotService.resetResultQuestion(json, chatId, trainer)
            CALLBACK_DATA_RESET_RESULT -> {
                trainer.resetResult()
                telegramBotService.sendMessageAndMenuBackButton(json, "☑\uFE0F Ваш результат сброшен", chatId)
            }
        }

        if (callbackData.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
            checkAnswer(callbackData, trainer, telegramBotService, chatId)
            checkNextQuestionAndSend(json, trainer, telegramBotService, chatId)
        }
    }
}

fun checkNextQuestionAndSend(
    json: Json,
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
) {
    val question = trainer.getNextQuestion()
    if (trainer.isDictionaryEmpty) {
        telegramBotService.sendMessageAndMenuBackButton(json, "\uD83D\uDD5C Невозможно загрузить словарь", chatId)
        return
    }
    if (question == null) {
        telegramBotService.sendMessageAndMenuBackButton(json,"✅ Все слова в словаре выучены", chatId)
        return
    } else telegramBotService.sendQuestion(json, chatId, question)
}

fun checkAnswer(
    callbackData: String,
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long)
{
    val userAnswerIndex = callbackData.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
    val correctAnswer = trainer.getCurrentQuestion()?.correctAnswer
    correctAnswer?.let {
        val resultMessage = if (trainer.checkAnswer(userAnswerIndex)) "✅ Правильно!"
        else "❌ Неправильно! ${correctAnswer.original} – это ${correctAnswer.translate}"
        telegramBotService.sendMessage(chatId, resultMessage)
    } ?: telegramBotService.sendMessage(chatId, "\uD83D\uDD5C Ответ на вопрос не загрузился")
}