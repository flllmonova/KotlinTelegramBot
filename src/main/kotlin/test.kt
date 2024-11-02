package org.example

fun main() {
//    val num = 2
    println(rowSumOddNumbers(3))
    println(rowSumOddNumbers(4))
    println(rowSumOddNumbers(5))
}

fun rowSumOddNumbers(n: Int): Int =
    (1 until n).sum() * 2 + 1