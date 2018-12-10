package day10

import java.io.File
import kotlin.math.abs

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

private const val NEIGHBOUR_COUNT = 8

data class Sky(val points: List<Point>, var clock: Int = 0) {

    fun findMessage() = buildString {
        while (!points.haveVerticalLine()) {
            tick()
        }
        val neighbours = points.neighbours()
        val xRange = neighbours.map { it.x }.let { it.min()!!..it.max()!! }
        val yRange = neighbours.map { it.y }.let { it.min()!!..it.max()!! }
        append('\n')
        for (y in yRange) {
            for (x in xRange) {
                if (neighbours.any { it.x == x && it.y == y }) {
                    append('#')
                } else {
                    append(' ')
                }
            }
            append('\n')
        }
    }

    private fun tick() {
        points.forEach { it.update() }
        clock++
    }

    private fun List<Point>.haveVerticalLine() = any { point ->
        count { it.x == point.x && (it.y - point.y) in 1..NEIGHBOUR_COUNT } == NEIGHBOUR_COUNT
    }

    private fun List<Point>.neighbours() = filter { point -> count { abs(it.x - point.x) < NEIGHBOUR_COUNT || abs(it.y - point.y) < NEIGHBOUR_COUNT } > NEIGHBOUR_COUNT }

}

data class Point(var x: Int, var y: Int, val vx: Int, val vy: Int) {
    fun update() {
        x += vx
        y += vy
    }
}

private val lineStructure = """position=<([ -]*\d+),([ -]*\d+)> velocity=<([ -]*\d+),([ -]*\d+)>""".toRegex()

fun parse(input: List<String>) = input.map {
    lineStructure.matchEntire(it)?.destructured?.let {
        val (x, y, vx, vy) = it.toList().map { it.trim().toInt() }
        Point(x, y, vx, vy)
    }
}.requireNoNulls().let { Sky(it) }

fun part1(input: Sky) = input.findMessage()

fun part2(input: Sky) = input.clock
