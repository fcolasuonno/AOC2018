package day6

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

data class Voronoi(val points: List<Coord>) {
    val borders = mutableSetOf<Int>()
    val sizeX = 2 + points.map { it.x }.max()!!
    val sizeY = 2 + points.map { it.y }.max()!!
    val grid = MutableList(sizeX) { x ->
        MutableList(sizeY) { y ->
            points.closest(x, y)?.index?.let {
                if (y == 0 || y == sizeY - 1 || x == 0 || x == sizeX - 1) {
                    borders.add(it)
                }
                it
            }
        }
    }
}

data class DistanceMap(val points: List<Coord>, val maxDistance: Int) {
    val sizeX = 2 + points.map { it.x }.max()!!
    val sizeY = 2 + points.map { it.y }.max()!!
    val grid = MutableList(sizeX) { x ->
        MutableList(sizeY) { y ->
            points.sumBy { it.distance(x, y) } < maxDistance
        }
    }

}

private fun List<Coord>.closest(x: Int, y: Int) = sortedBy { it.distance(x, y) }.let {
    if (it[0].distance(x, y) == it[1].distance(x, y)) null else it.first()
}

data class Coord(val x: Int, val y: Int, val index: Int) {
    fun distance(x: Int, y: Int) = abs(this.x - x) + abs(this.y - y)
}

private val lineStructure = """(\d+), (\d+)""".toRegex()

fun parse(input: List<String>) = input.mapIndexed { index, it ->
    lineStructure.matchEntire(it)?.destructured?.let {
        val (x, y) = it.toList().map { it.toInt() }
        Coord(x, y, index)
    }
}.requireNoNulls()

fun part1(input: List<Coord>): Int? {
    val voronoi = Voronoi(input)
    return voronoi.grid.flatMap { it.filterNot { it == null || voronoi.borders.contains(it) } }
            .groupingBy { it }.eachCount().values.max()
}

fun part2(input: List<Coord>) = DistanceMap(input, 10000).grid.sumBy { it.count { it } }
