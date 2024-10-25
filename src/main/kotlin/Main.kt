package org.example

import java.io.File

const val REQUIRED_LEARNED_COUNT = 3
const val QUESTION_WORDS_COUNT = 4

fun main() {

    val dictionary = loadDictionary(File("words.txt"))

    do {
        println(
            """
            Меню: 
            1 – Учить слова
            2 – Статистика
            0 – Выход
        """.trimIndent()
        )

        val userInput = readln().toIntOrNull()

        when (userInput) {
            1 -> {
                do {
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < REQUIRED_LEARNED_COUNT }

                    if (notLearnedList.isEmpty()) {
                        println("Все слова в словаре выучены\n")
                        break
                    }

                    val questionWords = notLearnedList.shuffled().take(QUESTION_WORDS_COUNT)
                    val correctAnswer = questionWords.random()

                    println("\n${correctAnswer.original}:")
                    questionWords.forEachIndexed { index, word ->
                        println(" ${index + 1} - ${word.translate}")
                    }

                    val userAnswerInput = readln().toIntOrNull()
                } while (userAnswerInput != 0)
            }

            2 -> {
                val totalCount = dictionary.size
                val learnedCount = dictionary.filter { it.correctAnswersCount >= REQUIRED_LEARNED_COUNT }.size
                val percent = learnedCount * 100 / totalCount
                println("Выучено $learnedCount из $totalCount слов | $percent%\n")
            }

            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    } while (true)
}

fun loadDictionary(wordsFile: File): List<Word> {

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