package day1

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

fun parse(input: List<String>) = input.map { it.toInt() }

fun part1(input: List<Int>) = input.sum()

fun part2(input: List<Int>): Int {
    val frequencies = mutableSetOf<Int>()
    var frequency = 0
    while (true) {
        for (f in input) {
            frequency += f
            if (!frequencies.add(frequency)) return frequency
        }
    }
}
