package org.example

import java.io.File

const val REQUIRED_LEARNED_COUNT = 3
const val QUESTION_WORDS_COUNT = 4

class LearnWordsTrainer {

    private val dictionary = loadDictionary()
    private var question: Question? = null

    private fun loadDictionary(): List<Word> {
        val dictionary: MutableList<Word> = mutableListOf()
        val wordsFile = File("words.txt")
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

    private fun saveDictionary(words: List<Word>) {
        val wordsFile = File("words.txt")
        wordsFile.writeText("")
        words.forEach { wordsFile.appendText("${it.component1()}|${it.component2()}|${it.component3()}\n") }
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < REQUIRED_LEARNED_COUNT }
        if (notLearnedList.isEmpty()) return null
        val questionWords = notLearnedList.take(QUESTION_WORDS_COUNT).shuffled()
        val correctAnswer = questionWords.random()
        question = Question(variants = questionWords, correctAnswer = correctAnswer)
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerIndex = it.variants.indexOf(it.correctAnswer)
            if (userAnswerIndex == correctAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    fun getStatistics(): Statistics {
        val total = dictionary.size
        val learned = dictionary.filter { it.correctAnswersCount >= REQUIRED_LEARNED_COUNT }.size
        val percent = learned * 100 / total
        return Statistics(total, learned, percent)
    }

    fun printMenu() {
        println(
            """
            Меню: 
            1 – Учить слова
            2 – Статистика
            0 – Выход
        """.trimIndent()
        )
    }
}