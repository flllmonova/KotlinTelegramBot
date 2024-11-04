package org.example

import java.io.File

class LearnWordsTrainer(
    private val learnedAnswerCount: Int = 3,
    private val questionWordCount: Int = 4,
) {
    private val dictionary = loadDictionary()
    val isDictionaryEmpty = dictionary.isEmpty()

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
                saveDictionary(dictionary)
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