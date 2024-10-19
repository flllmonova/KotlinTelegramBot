package org.example

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    wordsFile.createNewFile()

    wordsFile.writeText("""
        hello привет
        dog собака
        cat кошка
    """.trimIndent())

    for (line in wordsFile.readLines()) {
        println(line)
    }
}