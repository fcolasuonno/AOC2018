package day3

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val cloth = Cloth()
    val parsed = parse(input, cloth)
    println("Part 1 = ${part1(cloth)}")
    println("Part 2 = ${part2(parsed, cloth)}")
}

data class Cloth(val surface: MutableMap<Int, MutableMap<Int, MutableList<Claim>>> = mutableMapOf()) {
    fun get(x: Int, y: Int) = surface.getOrPut(x) {
        mutableMapOf()
    }.getOrPut(y) {
        mutableListOf()
    }

    fun check() = surface.values.sumBy { it.values.count { it.size > 1 } }

    fun overlapping(): MutableSet<Int> {
        val overlappingId = mutableSetOf<Int>()

        surface.values.forEach {
            it.values.filter { it.size > 1 }.forEach {
                it.forEach {
                    overlappingId.add(it.id)
                }
            }
        }

        return overlappingId
    }
}

data class Claim(val id: Int, val x: Int, val y: Int, val width: Int, val height: Int) {
    fun assign(cloth: Cloth) {
        for (i in x until (x + width)) {
            for (j in y until (y + height)) {
                cloth.get(i, j).add(this)
            }
        }
    }
}

private val lineStructure = """#(\d+) @ (\d+),(\d+): (\d+)x(\d+)""".toRegex()

fun parse(input: List<String>, cloth: Cloth) = input.map {
    lineStructure.matchEntire(it)?.destructured?.let {
        val (id, x, y, w, h) = it.toList().map { it.toInt() }
        Claim(id, x, y, w, h).also { it.assign(cloth) }
    }
}.requireNoNulls()

fun part1(cloth: Cloth) = cloth.check()

fun part2(input: List<Claim>, cloth: Cloth) = input.map { it.id } - cloth.overlapping()