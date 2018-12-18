package day18

import printWith
import java.io.File
import java.util.*

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

data class Forest(var current: List<MutableList<Char>>) {
    override fun toString() = current.printWith(false) { it.toString() }
    fun step() {
        current = List(current.size) { y ->
            MutableList(current[0].size) { x ->
                rules(current[y][x], neighbours(current, x, y))
            }
        }
    }

    private fun rules(c: Char, neighbours: List<Char>): Char {
        return when (c) {
            '.' -> if (neighbours.count { it == '|' } >= 3) '|' else '.'
            '|' -> if (neighbours.count { it == '#' } >= 3) '#' else '|'
            '#' -> if (neighbours.any { it == '#' } && neighbours.any { it == '|' }) '#' else '.'
            else -> throw InputMismatchException()
        }
    }

    private fun neighbours(grid: List<MutableList<Char>>, x: Int, y: Int) = listOfNotNull(
            grid.getOrNull(y - 1)?.getOrNull(x - 1),
            grid.getOrNull(y - 1)?.getOrNull(x),
            grid.getOrNull(y - 1)?.getOrNull(x + 1),
            grid.getOrNull(y)?.getOrNull(x - 1),
            grid.getOrNull(y)?.getOrNull(x + 1),
            grid.getOrNull(y + 1)?.getOrNull(x - 1),
            grid.getOrNull(y + 1)?.getOrNull(x),
            grid.getOrNull(y + 1)?.getOrNull(x + 1)
    )

    fun value() = current.sumBy { it.count { it == '|' } } * current.sumBy { it.count { it == '#' } }

}

fun parse(input: List<String>) = input.map {
    it.toCharArray().toMutableList()
}.let { grid ->
    Forest(grid)
}

fun part1(input: Forest) = input.copy(current = input.current.map { it.map { it }.toMutableList() }).apply {
    repeat(10) {
        step()
    }
}.value()

fun part2(input: Forest): Int {
    repeat(5000) {
        input.step()
    }
    var period = 0
    val initial = input.value()
    do {
        period++
        input.step()
    } while (initial != input.value())
    repeat(((1000000000 - 5000) % period)) {
        input.step()
    }
    return input.value()
}
