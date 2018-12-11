package day11

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed, 3)}")
    println("Part 2 = ${part2(parsed)}")
}

fun parse(input: List<String>) = input.map {
    it.toInt()
}.first()

private fun createSquare(serial: Int): List<List<Int>> {
    return List(300 + 1) { x ->
        List(300 + 1) { y ->
            powerLevel(x, y, serial)
        }
    }
}

fun powerLevel(x: Int, y: Int, serial: Int): Int {
    val rackId = x + 10
    val powerStarts = rackId * y
    val addingSerial = powerStarts + serial
    val multiply = addingSerial * rackId
    val hundreds = multiply.toString().toCharArray().reversedArray().getOrNull(2)?.minus('0') ?: 0
    return hundreds - 5
}

fun sumPowerCells(
    x: Int,
    y: Int,
    input: List<List<Int>>,
    size: Int
): Int {
    var acc = 0
    for (i in x..(x + size - 1)) {
        for (j in y..(y + size - 1)) {
            acc += input[i][j]
        }
    }
    return acc
}

fun part1(input: Int, size: Int): Pair<Pair<Int, Int>, Int> {
    val square = createSquare(input)
    var max = Int.MIN_VALUE
    var currentMax = Pair(0, 0)
    for (x in 1..(300 - size + 1)) {
        for (y in (1..(300 - size + 1))) {
            val sum = sumPowerCells(x, y, square, size)
            if (sum > max) {
                max = sum
                currentMax = Pair(x, y)
            }
        }
    }
    return Pair(currentMax, max)
}

fun part2(input: Int) = (1 until 300).map {
    val square = createSquare(input)
    var max = Int.MIN_VALUE
    var currentMax = Pair(0, 0)
    for (x in 1..(300 - it + 1)) {
        for (y in (1..(300 - it + 1))) {
            val sum = sumPowerCells(x, y, square, it)
            if (sum > max) {
                max = sum
                currentMax = Pair(x, y)
            }
        }
    }
    Pair(Triple(currentMax.first, currentMax.second, it), max)
}.maxBy {
    it.second
}
