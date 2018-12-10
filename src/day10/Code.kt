package day10

import java.io.File
import kotlin.math.abs

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
//    println("Part 2 = ${part2(parsed)}")
}

data class Sky(val points: List<Point>) {
    init {
        val a = points.map { it.x / it.vx } + points.map { it.y / it.vy }
    }

    val minX = -20
    val minY = -20

    val maxX = 20
    val maxY = 20

    var distance = points.sumBy { abs(it.x) + abs(it.y) }
    fun print() {
        points.forEach { it.fixupdate(10000) }

        var delta = 0
        do {
            points.forEach { it.update() }
            val newDistance = points.sumBy { abs(it.x) + abs(it.y) }
            System.err.println(newDistance)
            delta = distance - newDistance
            distance = newDistance

        } while (delta > 0)
        points.forEach { it.revert() }
        points.forEach { it.revert() }
        points.forEach { it.revert() }
        repeat(10) {
            for (y in points.map { it.y }.let { it.min()!! until it.max()!! }) {
                for (x in points.map { it.x }.let { it.min()!! until it.max()!! }) {

                    if (points.any { it.x == x && it.y == y }) {
                        print("#")
                    } else {
                        print('.')
                    }
                }
                println()
            }
            points.forEach { it.update() }
            println()
            println()
            println()
            println()
            println()
        }
    }
}

data class Point(var x: Int, var y: Int, val vx: Int, val vy: Int) {
    fun update() {
        x += vx
        y += vy
    }

    fun revert() {
        x -= vx
        y -= vy
    }

    fun fixupdate(ff: Int) {
        x += (ff * vx)
        y += (ff * vy)
    }
}

private val lineStructure = """position=<([ -]*\d+),([ -]*\d+)> velocity=<([ -]*\d+),([ -]*\d+)>""".toRegex()

fun parse(input: List<String>) = input.map {
    lineStructure.matchEntire(it)?.destructured?.let {
        val (x, y, vx, vy) = it.toList().map { it.trim().toInt() }
        Point(x, y, vx, vy)
    }
}.requireNoNulls().let { Sky(it) }

fun part1(input: Sky) = input.print()

//fun part2(input: SomeState) = state.s
