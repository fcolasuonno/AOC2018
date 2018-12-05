package day5

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

fun parse(input: List<String>) = input.first().toList()

fun part1(input: List<Char>): Int {
    val reduced = input.toMutableList()
    var doWork: Boolean
    do {
        doWork = false
        for (i in (reduced.size - 2) downTo 0) {
            val next = reduced.getOrNull(i + 1)
            if (reduced[i].toLowerCase() == next?.toLowerCase()
                    && reduced[i] != next) {
                reduced.removeAt(i)
                reduced.removeAt(i)
                doWork = true
            }
        }
    } while (doWork)
    return reduced.size
}

fun part2(input: List<Char>) = ('a'..'z').map { c ->
    part1(input.filterNot { it.toLowerCase() == c })
}.min()

