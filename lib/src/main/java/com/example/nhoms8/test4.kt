package com.example.nhoms8

import kotlin.math.sqrt

fun kiemTraSoNguyenTo(n: Int): Boolean {
    if (n < 2) return false
    val limit = sqrt(n.toDouble()).toInt()
    for (i in 2..limit) {
        if (n % i == 0) return false
    }
    return true
}

fun main() {
    println("=== CHƯƠNG TRÌNH KIỂM TRA SỐ NGUYÊN TỐ ===")
    print("Nhập số n cần kiểm tra: ")
    val n = readln().toIntOrNull() ?: 0

    if (kiemTraSoNguyenTo(n)) {
        println("=> Số $n LÀ số nguyên tố.")
    } else {
        println("=> Số $n KHÔNG PHẢI là số nguyên tố.")
    }
}
