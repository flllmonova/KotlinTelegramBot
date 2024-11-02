package org.example

fun main() {

    val trainer = LearnWordsTrainer()

    while (true) {
        trainer.printMenu()

        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Все слова в словаре выучены\n")
                        break
                    }
                    println(question.questionToString())
                    val userAnswerInput = readln().toIntOrNull()
                    if (userAnswerInput == 0) {
                        println()
                        break
                    }

                    if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                        println("Правильно!")
                    } else {
                        println(
                            "Неправильно! " +
                            "${question.correctAnswer.original} это - ${question.correctAnswer.translate}"
                        )
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println(statistics.statisticsToString())
            }

            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}