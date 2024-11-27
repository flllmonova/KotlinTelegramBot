import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

private const val TELEGRAM_API = "https://api.telegram.org/bot"
const val CALLBACK_DATA_LEARNED_WORDS = "learn_words_clicked"
const val CALLBACK_DATA_STATISTICS = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val CALLBACK_DATA_MENU_BACK = "menu_back"
const val CALLBACK_DATA_RESET_RESULT_QUESTION = "reset_result_question"
const val CALLBACK_DATA_RESET_RESULT = "reset_result"

@Serializable
data class InlineMarkup(
    @SerialName("text")
    val text: String,
    @SerialName("callback_data")
    val callbackData: String,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineMarkup: List<List<InlineMarkup>>,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup,
)

class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder().build()
    val json = Json { ignoreUnknownKeys = true }
    private val menuBackButton = InlineMarkup(text = "↩\uFE0F В меню", callbackData = CALLBACK_DATA_MENU_BACK)

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$TELEGRAM_API$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, message: String) {
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8)
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMenu(chatId: Long) {
        val sendMessage = "$TELEGRAM_API$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(InlineMarkup(text = "📚 Изучить слова", callbackData = CALLBACK_DATA_LEARNED_WORDS)),
                    listOf(InlineMarkup(text = "\uD83D\uDCCA Статистика", callbackData = CALLBACK_DATA_STATISTICS)),
                    listOf(
                        InlineMarkup(
                            text = "\uD83D\uDD04 Сбросить результат",
                            callbackData = CALLBACK_DATA_RESET_RESULT_QUESTION
                        )
                    ),
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun sendQuestion(chatId: Long, question: Question) {
        val answerOptionsAndBackToMenuButton = question.variants.mapIndexed { index, word ->
            listOf(InlineMarkup(text = word.translate, callbackData = CALLBACK_DATA_ANSWER_PREFIX + index))
        }.toMutableList()
        answerOptionsAndBackToMenuButton.add(listOf(menuBackButton))

        val sendMessage = "$TELEGRAM_API$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "\uD83D\uDD35 ${question.correctAnswer.original} - это",
            replyMarkup = ReplyMarkup(answerOptionsAndBackToMenuButton)
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMessageAndMenuBackButton(text: String, chatId: Long) {
        val sendMessage = "$TELEGRAM_API$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text,
            replyMarkup = ReplyMarkup(listOf(listOf(menuBackButton)))
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun resetProgressQuestion(chatId: Long, trainer: LearnWordsTrainer) {
        val statistics = trainer.getStatistics()
        val requestBody = if (statistics.learned == 0) {
            SendMessageRequest(
                chatId = chatId,
                text = """
                Вы выучили ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%
                ❌ Сброс прогресса невозможен
            """.trimIndent(),
                replyMarkup = ReplyMarkup(listOf(listOf(menuBackButton)))
            )
        } else {
            SendMessageRequest(
                chatId = chatId,
                text = """
                Вы выучили ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%
                Сбросить прогресс?
            """.trimIndent(),
                ReplyMarkup(
                    listOf(
                        listOf(
                            InlineMarkup(text = "Да", callbackData = CALLBACK_DATA_RESET_RESULT),
                            InlineMarkup(text = "Нет", callbackData = CALLBACK_DATA_MENU_BACK)
                        ),
                        listOf(menuBackButton),
                    )
                )
            )
        }
        val sendMessage = "$TELEGRAM_API$botToken/sendMessage"
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }
}