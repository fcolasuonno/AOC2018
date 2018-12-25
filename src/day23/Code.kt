package day23

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

data class Position(val x: Int, val y: Int, val z: Int) {
    fun distance(pos: Position): Int = abs(pos.x - x) + abs(pos.y - y) + abs(pos.z - z)
    fun grid(newSize: Int) = listOf(copy(x = x - newSize), copy(x = x), copy(x = x + newSize))
            .flatMap { listOf(it.copy(y = y - newSize), it.copy(y = y), it.copy(y = y + newSize)) }
            .flatMap { listOf(it.copy(z = z - newSize), it.copy(z = z), it.copy(z = z + newSize)) }
}

data class Drone(val pos: Position, val r: Int)

private val lineStructure = """pos=<(-?\d+),(-?\d+),(-?\d+)>, r=(\d+)""".toRegex()

fun parse(input: List<String>) = input.map {
    lineStructure.matchEntire(it)?.destructured?.let {
        val (x, y, z, r) = it.toList().map { it.toInt() }
        Drone(Position(x, y, z), r)
    }
}.requireNoNulls()

fun part1(input: List<Drone>): Int {
    val powerful = input.maxBy { it.r }!!
    return input.count { it.pos.distance(powerful.pos) <= powerful.r }
}

data class TestPoint(val pos: Position, val r: Int) {
    fun countIntersection(input: List<Drone>) = input.count { it.pos.distance(pos) <= (it.r + r) }

    fun split(): List<TestPoint> {
        val newSize = r / 2
        return pos.grid(newSize).map { TestPoint(it, newSize) }
    }
}

fun part2(input: List<Drone>): Any {
    val zero = Position(0, 0, 0)

    var testPoints = TestPoint(Position(0, 0, 0),
            maxOf(
                    abs(input.map { it.pos.x }.let { it.max()!! - it.min()!! }),
                    abs(input.map { it.pos.y }.let { it.max()!! - it.min()!! }),
                    abs(input.map { it.pos.z }.let { it.max()!! - it.min()!! })))
            .split()
            .topValues { it.countIntersection(input) }
    while (testPoints.first().r != 1) {
        testPoints = testPoints.flatMap { it.split() }.topValues { it.countIntersection(input) }
    }
    return testPoints.topValues { t -> input.count { it.pos.distance(t.pos) <= it.r } }.map { it.pos.distance(zero) }.min()!!
}

private fun <E> List<E>.topValues(groupingFunction: (E) -> Int) =
        this.groupBy(groupingFunction).entries.sortedByDescending { it.key }.first().value