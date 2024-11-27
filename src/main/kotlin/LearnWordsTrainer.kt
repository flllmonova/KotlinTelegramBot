import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

data class Statistics(
    val total: Int,
    val learned: Int,
    val percent: Int,
)

fun Statistics.statisticsToString(): String {
    return "✅ Выучено ${this.learned} из ${this.total} слов | ${this.percent}%\n"
}

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val learnedAnswerCount: Int = 3,
    private val questionWordCount: Int = 4,
) {
    private val dictionary = loadDictionary()
    val isDictionaryEmpty = dictionary.isEmpty()

    private var question: Question? = null
    fun getCurrentQuestion(): Question? = question

    private fun loadDictionary(): List<Word> {
        val wordsFile = File(fileName)
        if (!wordsFile.exists()) File("words.txt").copyTo(wordsFile)

        val dictionary: MutableList<Word> = mutableListOf()
        wordsFile.forEachLine {
            val line = it.split("|")
            val word = Word(
                original = line[0],
                translate = line[1],
                correctAnswersCount = line.getOrNull(2)?.toIntOrNull() ?: 0
            )
            dictionary.add(word)
        }
        return dictionary
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }

    private fun saveDictionary() {
        val wordsFile = File(fileName)
        wordsFile.writeText("")
        dictionary.forEach { wordsFile.appendText("${it.component1()}|${it.component2()}|${it.component3()}\n") }
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (notLearnedList.isEmpty()) return null

        val questionWords = if (notLearnedList.size < questionWordCount) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }
            (notLearnedList + learnedList.shuffled().take(questionWordCount - notLearnedList.size)).shuffled()
        } else {
            notLearnedList.shuffled().take(questionWordCount)
        }

        val correctAnswer = questionWords.random()

        question = Question(variants = questionWords, correctAnswer = correctAnswer)
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerIndex = it.variants.indexOf(it.correctAnswer)
            if (userAnswerIndex == correctAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    fun getStatistics(): Statistics {
        val total = dictionary.size
        val learned = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.size
        val percent = learned * 100 / total
        return Statistics(total, learned, percent)
    }
}