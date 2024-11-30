package org.example

fun main() {

    val trainer = try {
        LearnWordsTrainer("words.txt", 3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (trainer.isDictionaryEmpty) {
                        println("Невозможно загрузить словарь")
                        break
                    }

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
                try {
                    val statistics = trainer.getStatistics()
                    println(statistics.statisticsToString())
                } catch (e: Exception) {
                    println("Невозможно загрузить статистику")
                }
            }

            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}