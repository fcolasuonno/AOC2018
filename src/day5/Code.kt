package day5

import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println(measureTimeMillis {
        println("Part 1 = ${part1(parsed)}")
    })
    println(measureTimeMillis {
        println("Part 2 = ${part2(parsed)}")
    })
}

fun parse(input: List<String>) = input.first().toList()

fun part1(input: List<Char>, exclude: Char? = null): Int {
    val stack = ArrayDeque<Char>(input.size)
    for (c in input) {
        val lowerCased = c.toLowerCase()
        if (lowerCased == exclude) {
            continue
        }
        if (lowerCased == stack.peek()?.toLowerCase() && c != stack.peek()) {
            stack.pop()
        } else {
            stack.push(c)
        }
    }
    return stack.size
}

fun part2(input: List<Char>) = ('a'..'z').map { c ->
    part1(input, c)
}.min()

