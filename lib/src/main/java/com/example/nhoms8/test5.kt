package com.example.nhoms8

fun tinhTongTu1DenN(n: Int): Int {
    var sum = 0
    for (i in 1..n) {
        sum += i
    }
    return sum
}

fun main() {
    println("=== CHƯƠNG TRÌNH TÍNH TỔNG TỪ 1 ĐẾN N ===")
    print("Nhập số nguyên n: ")
    val n = readln().toIntOrNull() ?: 0

    if (n < 1) {
        println("Vui lòng nhập n là số nguyên dương lớn hơn 0!")
    } else {
        val sum = tinhTongTu1DenN(n)
        println("=> Tổng S = 1 + 2 + ... + $n là: $sum")
    }
}
