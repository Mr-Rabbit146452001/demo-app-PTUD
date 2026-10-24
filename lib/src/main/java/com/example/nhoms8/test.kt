package com.example.nhoms8

fun main() {
    println("Nhóm 08")
    print("Nhập số phần tử: ")
    val n = readln().toIntOrNull() ?: 0

    if (n <= 0) {
        println("Số phần tử phải lớn hơn 0!")
        return
    }

    // Khởi tạo mảng số nguyên gồm n phần tử
    val arr = IntArray(n)

    // Nhập từng phần tử an toàn
    for (i in 0 until n) {
        print("Nhập arr[$i]: ")
        arr[i] = readln().toIntOrNull() ?: 0
    }

    // In mảng vừa nhập
    println("Mảng vừa nhập: ${arr.joinToString(", ")}")
}
