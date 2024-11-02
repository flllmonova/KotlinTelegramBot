package org.example

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

fun Question.questionToString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> " ${index + 1} - ${word.translate}" }
        .joinToString("\n")
    return "\n${this.correctAnswer.original}: \n" + variants + "\n ----------" + "\n 0 - Меню"
}