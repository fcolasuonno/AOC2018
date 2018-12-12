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
    var state = List(NegativeConsidered) { false } + input.initial
    val box = LinearIntegerRegression(10)
    while (box.error > 0.1) {
        state = (listOf(false, false) + state + listOf(false, false, false)).windowed(5, 1) {
            input.rules.contains(it)
        }
        box.add(state.indices.filter { state[it] }.map { it - NegativeConsidered }.sum().toLong())
    }
    return box.predict(50000000000L - 1)
}

class LinearRegression(private val sampleNum: Int) {
    private val samples = mutableListOf<Pair<Double, Double>>()
    private var currX = 0
    var b = 0.0
        private set
    var a = 0.0
        private set
    var error = Double.MAX_VALUE
        private set

    fun add(y: Double) = add((currX++).toDouble(), y)

    fun add(x: Double, y: Double) {
        samples.add(Pair(x, y))
        if (samples.size > sampleNum) {
            samples.removeAt(0)
        }
        val n = samples.size
        if (n > 2) {

            val sxy = samples.sumByDouble { it.first * it.second }
            val sx = samples.sumByDouble { it.first }
            val sx2 = samples.sumByDouble { it.first * it.first }
            val sy = samples.sumByDouble { it.second }
            val sy2 = samples.sumByDouble { it.second * it.second }
            b = (n * sxy - sx * sy) / (n * sx2 - sx * sx)
            a = (sy - b * sx) / n
            error = (n * sy2 - sy * sy - b * b * n * sx2 + b * b * sx * sx) / (n * (n - 2))
        }
    }

    fun predict(x: Double): Double = a + b * x
}

class LinearIntegerRegression(private val sampleNum: Int) {
    private val samples = mutableListOf<Pair<Int, Long>>()
    private var currX = 0
    var b = 0.0
        private set
    var a = 0.0
        private set
    var error = Double.MAX_VALUE
        private set

    fun add(y: Long) = add(currX++, y)

    fun add(x: Int, y: Long) {
        samples.add(Pair(x, y))
        if (samples.size > sampleNum) {
            samples.removeAt(0)
        }
        val n = samples.size
        if (n > 2) {

            val sxy = samples.map { it.first * it.second }.sum()
            val sx = samples.sumBy { it.first }
            val sx2 = samples.sumBy { it.first * it.first }
            val sy = samples.map { it.second }.sum()
            val sy2 = samples.map { it.second * it.second }.sum()
            b = (n * sxy - sx * sy).toDouble() / (n * sx2 - sx * sx)
            a = (sy - b * sx) / n
            error = (n * sy2 - sy * sy - b * b * n * sx2 + b * b * sx * sx) / (n * (n - 2))
        }
    }

    fun predict(x: Long) = (a + b * x).toLong()
}