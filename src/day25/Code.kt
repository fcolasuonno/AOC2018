package day25

import java.io.File
import java.util.*
import kotlin.math.abs

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

data class Coord(val x: Int, val y: Int, val z: Int, val t: Int) {
    var constellation = Int.MAX_VALUE

    fun distance(c: Coord) = abs(c.x - x) + abs(c.y - y) + abs(c.z - z) + abs(c.t - t)
}

private val lineStructure = """(-?\d+),(-?\d+),(-?\d+),(-?\d+)""".toRegex()

fun parse(input: List<String>) = input.map {
    lineStructure.matchEntire(it)?.destructured?.let {
        val (x, y, z, t) = it.toList().map { it.toInt() }
        Coord(x, y, z, t)
    }
}.requireNoNulls()

fun part1(input: List<Coord>): Int {
    val edges = input.associateWith { c -> input.filter { it != c && it.distance(c) <= 3 } }.toMutableMap()
    var constellationNumber = 1
    val constellationMap = mutableMapOf<Coord, Int>()
    val frontier = ArrayDeque<Coord>()
    frontier.addAll(input)
    while (frontier.isNotEmpty()) {
        val current = frontier.pop()
        val currentConstellation = constellationMap[current]
        current.constellation = currentConstellation ?: (constellationNumber++)
        constellationMap[current] = current.constellation
        edges[current]?.forEach {
            frontier.push(it)
            constellationMap[it] = current.constellation
        }
        edges.remove(current)
    }
    return constellationNumber - 1
}

fun part2(input: List<Coord>) = input.size
