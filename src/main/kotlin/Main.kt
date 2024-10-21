package org.example

import java.io.File

fun main() {

    val dictionary = loadDictionary(File("words.txt"))

    dictionary.forEach { println(it.toString()) }
    println()

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
            1 -> println("Выбран пункт 'Учить слова'")
            2 -> println("Выбран пункт 'Статистика'")
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