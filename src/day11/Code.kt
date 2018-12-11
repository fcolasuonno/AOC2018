package day11

import java.io.File
import kotlin.system.measureTimeMillis

private const val gridSize = 300

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println(measureTimeMillis {
        println("Part 1 = ${part1(parsed)}")
        println("Part 2 = ${part2(parsed)}")
    })
}

fun parse(input: List<String>) = input.map {
    it.toInt()
}.first().let {
    createSquare(it)
}.let {
    generatePyramid(it)
}

fun powerLevel(x: Int, y: Int, serial: Int): Int {
    val rackId = (x + 1) + 10
    val hundreds = (((rackId * (y + 1) + serial) * rackId) / 100) % 10
    return hundreds - 5
}

fun createSquare(serial: Int) = List(gridSize) { x ->
    List(gridSize) { y ->
        powerLevel(x, y, serial)
    }
}

fun generatePyramid(square: List<List<Int>>) = generateSequence(square) {
    val size = square.size - it.size + 1
    List(it.size - 1) { x ->
        List(it.size - 1) { y ->
            val rightSide = (0 until size).sumBy { square[x + size][y + it] }
            val lowerSide = (0 until size).sumBy { square[x + it][y + size] }
            it[x][y] + rightSide + lowerSide + square[x + size][y + size]
        }
    }
}

fun findMax(grid: List<List<Int>>): Pair<Int, Int> {
    var max = Int.MIN_VALUE
    var currentMax = Pair(0, 0)
    for (x in 0 until grid.size) {
        for (y in 0 until grid.size) {
            val gridValue = grid[x][y]
            if (gridValue > max) {
                max = gridValue
                currentMax = Pair(x + 1, y + 1)
            }
        }
    }
    return currentMax
}

fun part1(input: Sequence<List<List<Int>>>) = findMax(input.take(3).last())

fun findTotalMax(pyramid: List<List<List<Int>>>): Triple<Int, Int, Int> {
    var max = Int.MIN_VALUE
    var currentMax = Triple(0, 0, 0)
    for (size in 1 until pyramid.size) {
        val grid = pyramid[size]
        for (x in 0 until grid.size) {
            for (y in 0 until grid.size) {
                val gridValue = grid[x][y]
                if (gridValue > max) {
                    max = gridValue
                    currentMax = Triple(x + 1, y + 1, size + 1)
                }
            }
        }
    }
    return currentMax
}

fun part2(input: Sequence<List<List<Int>>>) = findTotalMax(input.takeWhile { it.isNotEmpty() }.toList())