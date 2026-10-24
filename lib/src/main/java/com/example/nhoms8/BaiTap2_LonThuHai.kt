package com.example.nhoms8

fun main() {
    println("=== BÀI TẬP 2: TÌM PHẦN TỬ LỚN THỨ 2 TRONG MẢNG ===")
    print("Nhập số phần tử của mảng: ")
    val n = readln().toIntOrNull() ?: 0

    if (n < 2) {
        println("Mảng phải có ít nhất 2 phần tử để tìm phần tử lớn thứ 2!")
        return
    }

    // Khởi tạo mảng n phần tử
    val arr = IntArray(n)
    for (i in 0 until n) {
        print("Nhập arr[$i]: ")
        arr[i] = readln().toIntOrNull() ?: 0
    }

    println("\nMảng vừa nhập: ${arr.joinToString(", ")}")

    // Thuật toán tìm phần tử lớn nhất (max1) và lớn thứ hai (max2)
    var max1 = Int.MIN_VALUE
    var max2 = Int.MIN_VALUE

    for (x in arr) {
        if (x > max1) {
            max2 = max1
            max1 = x
        } else if (x > max2 && x != max1) {
            max2 = x
        }
    }

    if (max2 == Int.MIN_VALUE) {
        println("=> Tất cả các phần tử trong mảng đều bằng nhau, không có phần tử lớn thứ 2.")
    } else {
        println("=> Phần tử lớn nhất là: $max1")
        println("=> Phần tử lớn thứ 2 là: $max2")
    }
}
