package com.example.nhoms8

/**
 * Bài tập 1: Viết chương trình nhập vào mảng và tính tổng các phần tử chẵn trong mảng.
 */
fun main() {
    println("=== BÀI TẬP 1: TÍNH TỔNG CÁC PHẦN TỬ CHẴN TRONG MẢNG ===")
    print("Nhập số phần tử của mảng: ")
    val n = readln().toIntOrNull() ?: 0

    if (n <= 0) {
        println("Số phần tử phải lớn hơn 0!")
        return
    }

    // Khởi tạo mảng n phần tử
    val arr = IntArray(n)
    for (i in 0 until n) {
        print("Nhập arr[$i]: ")
        arr[i] = readln().toIntOrNull() ?: 0
    }

    println("\nMảng vừa nhập: ${arr.joinToString(", ")}")

    // Tính tổng các phần tử chẵn
    var tongChan = 0
    val danhSachChan = mutableListOf<Int>()

    for (x in arr) {
        if (x % 2 == 0) {
            tongChan += x
            danhSachChan.add(x)
        }
    }

    if (danhSachChan.isEmpty()) {
        println("=> Không có phần tử chẵn nào trong mảng.")
    } else {
        println("=> Các phần tử chẵn là: ${danhSachChan.joinToString(", ")}")
        println("=> Tổng các phần tử chẵn = $tongChan")
    }
}
