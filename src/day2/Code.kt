package day2

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

fun parse(input: List<String>) = input

fun part1(input: List<String>) =
    input.map { it.groupBy { it } }.map {
        it.values.map { it.size }
    }.let {
        it.count { it.contains(2) } * it.count { it.contains(3) }
    }

fun part2(input: List<String>): String {
    input.forEach { s1 ->
        input.forEach { s2 ->
            if (s1.zip(s2).count { it.first != it.second } == 1) {
                return s1.zip(s2).filter { it.first == it.second }.unzip().first.joinToString("")
            }
        }
    }
    throw NotImplementedError()
}

