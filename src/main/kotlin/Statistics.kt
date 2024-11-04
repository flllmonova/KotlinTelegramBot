package org.example

class Statistics(
    val total: Int,
    val learned: Int,
    val percent: Int,
)

fun Statistics.statisticsToString(): String {
    return "Выучено ${this.learned} из ${this.total} слов | ${this.percent}%\n"
}