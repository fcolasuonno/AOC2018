package day9

import java.io.File
import java.util.*

private const val DEBUG = false
private const val DEBUG_PRINT = false

fun main(args: Array<String>) {
    val name = if (DEBUG) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    parsed.forEach {
        println("Part 1 = ${part1(it)}")
    }
    println("Part 2 = ${part2(parsed.first())}")
}

class Marbles {
    private val marbles = LinkedList<Int>().apply { this.add(0) }
    private var iterator = marbles.listIterator()

    fun clockWise(): Int {
        if (!iterator.hasNext()) {
            iterator = marbles.listIterator()
        }
        return iterator.next()
    }

    fun counterClockWise() {
        if (!iterator.hasPrevious()) {
            iterator = marbles.listIterator(marbles.size)
        }
        iterator.previous()
    }

    fun add(nextValue: Int) = iterator.add(nextValue)
    fun remove() = iterator.remove()

    override fun toString(): String {
        val current = iterator.nextIndex() - 1
        val before = marbles.slice(marbles.indices.filter { it < current }).joinToString(" ")
        val after = marbles.slice(marbles.indices.filter { it > current }).joinToString(" ")
        return "$before (${marbles[current]}) $after"
    }
}


data class Game(val players: Int, val lastPoint: Int, val marbles: Marbles = Marbles()) {
    var currentPlayer = -1

    fun play(): MutableMap<Int, Long> {
        val scores: MutableMap<Int, Long> = mutableMapOf()
        repeat(lastPoint) {
            currentPlayer = (currentPlayer + 1) % players
            val nextValue = it + 1
            if (DEBUG) {
                if (nextValue % 1_000_000 == 0) println(nextValue)
            }
            if (nextValue % 23 != 0) {
                marbles.clockWise()
                marbles.add(nextValue)
            } else {
                repeat(8) { marbles.counterClockWise() }
                scores[currentPlayer] = (scores.getOrDefault(currentPlayer, 0) + marbles.clockWise() + nextValue)
                marbles.remove()
                marbles.clockWise()
            }
            if (DEBUG_PRINT && nextValue < 50) {
                println("turn $nextValue [${currentPlayer + 1}] $marbles")
            }
        }
        return scores
    }
}

private val lineStructure = """(\d+) players; last marble is worth (\d+) points""".toRegex()

fun parse(input: List<String>) = input.map {
    lineStructure.matchEntire(it)?.destructured?.let {
        val (players, lastPoint) = it.toList().map { it.toInt() }
        Game(players, lastPoint)
    }
}.requireNoNulls()

fun part1(input: Game) = input.play().values.max()

fun part2(input: Game) = part1(input.copy(lastPoint = input.lastPoint * 100))
