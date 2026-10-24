package com.example.nhoms8

fun main() {
    println("Nhóm 08")
    println("nhập số a")
    val a = readln().toIntOrNull() ?: 0
    println("nhập số b")
    val b = readln().toIntOrNull() ?: 0

    println("$a + $b=${a + b}")
    println("$a - $b=${a - b}")
    println("$a * $b=${a * b}")
    println("$a / $b=${a / b}")
    println("$a % $b=${a % b}")
}
