package org.example

import java.io.File

fun main() {
    val wordsFile = File("words.txt")

    val dictionary: MutableList<Word> = mutableListOf()

    wordsFile.forEachLine {
        val line = it.split("|")
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }

    dictionary.forEach { println(it.toString()) }
}