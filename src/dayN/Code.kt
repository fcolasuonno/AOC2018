package dayN

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val someState = SomeState("a")
    val parsed = parse(input, someState)
    println("Part 1 = ${part1(parsed, someState)}")
    println("Part 2 = ${part2(parsed, someState)}")
}

data class SomeState(val s: String)

data class SomeObject(val i1: String)

private val lineStructure = """#(\d+) @ (\d+),(\d+): (\d+)x(\d+)""".toRegex()

fun parse(input: List<String>, state: SomeState) = input.map {
    lineStructure.matchEntire(it)?.destructured?.let {
        val (id) = it.toList()
        SomeObject(id)
    }
}.requireNoNulls()

fun part1(input: List<SomeObject>, state: SomeState) = state.s

fun part2(input: List<SomeObject>, state: SomeState) = state.s
