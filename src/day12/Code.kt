package day12

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

private const val NegativeConsidered = 3

class Life(val initial: List<Boolean>, val rules: Set<List<Boolean>>)

private val lineStructure = """([.#][.#][.#][.#][.#]) => ([.#])""".toRegex()

fun parse(input: List<String>) = input.partition { it.startsWith("initial") }.let {
    Life(
        it.first.single().let {
            "[^.#]+([.#]+)".toRegex().matchEntire(it)!!.destructured.component1().toCharArray().map { it == '#' }
        },
        it.second.map {
            lineStructure.matchEntire(it)?.destructured?.let {
                val (pattern, alive) = it.toList()
                pattern.toCharArray().map { it == '#' }.takeIf { alive == "#" }
            }
        }.filterNotNull().toSet()
    )
}

fun part1(input: Life): Int {
    var state = List(NegativeConsidered) { false } + input.initial
    repeat(20) {
        state = (listOf(false, false) + state + listOf(false, false, false)).windowed(5, 1) {
            input.rules.contains(it)
        }
    }
    return state.indices.filter { state[it] }.map { it - NegativeConsidered }.sum()
}

fun part2(input: Life): Long {
    var reachedStabilityCount = 0
    var prevIndices = emptyList<Int>()
    var newIndices = listOf(Int.MIN_VALUE)
    var state = List(NegativeConsidered) { false } + input.initial

    while (prevIndices != newIndices) {
        state = (listOf(false, false) + state + listOf(false, false, false)).windowed(5, 1) {
            input.rules.contains(it)
        }
        prevIndices = newIndices
        reachedStabilityCount++
        newIndices = state.indices.filter { state[it] }.map { it - reachedStabilityCount - NegativeConsidered }
    }
    return prevIndices.map { it + 50000000000L }.sum()
}
