package day8

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

class Zipper(val input: List<Int>) {
    var start: Int = 0
    fun traverse(): Node {
        val numChildren = input[start++]
        val numMeta = input[start++]
        val children = mutableListOf<Node>()
        val meta = mutableListOf<Int>()
        repeat(numChildren) {
            children.add(traverse())
        }
        repeat(numMeta) {
            meta.add(input[start++])
        }
        return Node(children, meta)
    }
}

data class Node(val children: List<Node>, val meta: List<Int>) {
    fun sumOfMeta(): Int = meta.sum() + children.sumBy { it.sumOfMeta() }

    fun value(): Int = if (children.isEmpty()) {
        meta.sum()
    } else {
        meta.sumBy { children.getOrNull(it - 1)?.value() ?: 0 }
    }
}

fun parse(input: List<String>) = input.first()
        .split(" ".toRegex())
        .map {
            it.toInt()
        }.let {
            Zipper(it).traverse()
        }


fun part1(input: Node) = input.sumOfMeta()

fun part2(input: Node) = input.value()
