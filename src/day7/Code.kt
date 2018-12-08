package day7

import java.io.File
import java.util.*

fun main(args: Array<String>) {
    val name = if (true) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

data class Step(val id: Char, val allow: Set<Char>)

private val lineStructure = """Step (.) must be finished before step (.) can begin.""".toRegex()

fun parse(input: List<String>) = input.map {
    lineStructure.matchEntire(it)?.destructured?.let {
        val (required, allowed) = it.toList()
        Pair(required.single(), allowed.single())
    }
}.requireNoNulls().groupBy { it.first }.mapValues {
    Step(it.key, it.value.map { it.second }.toSet())
}

fun part1(input: Map<Char, Step>): Any? {
    val output = mutableListOf<Char>()
    val remaining = input.toMutableMap()
    val available = sortedSetOf<Char>()
    while (remaining.isNotEmpty() || available.isNotEmpty()) {
        available.addAll(remaining.keys - remaining.values.flatMap { it.allow })

        available.pop(1).single().let {
            output.add(it)
            val allowed = remaining.remove(it)?.allow ?: emptySet()
            val notReady = remaining.values.flatMap { it.allow }.toSet()
            available.addAll(allowed - notReady)
        }
    }
    return output.joinToString("")
}

private fun <E> TreeSet<E>.pop(num: Int) = iterator().run {
    val out = mutableListOf<E>()
    repeat(num) {
        if (hasNext()) {
            out.add(next())
            remove()
        }
    }
    out
}

data class WorkingStep(val id: Char, val allow: Set<Char>) {
    var time: Int = id - 'A' + 1 + 60
    fun tick(onFinished: () -> Unit) {
        time--
        if (time == 0) {
            onFinished()
        }
    }
}

fun part2(input: Map<Char, Step>): Any? {
    val remaining = input.toMutableMap()
    val available = sortedSetOf<Char>()
    val workedOn = mutableListOf<WorkingStep>()
    var clock = -1
    var freeWorkers = 5
    while (remaining.isNotEmpty() || available.isNotEmpty() || workedOn.any { it.time != 0 }) {
        clock++
        val finished = mutableListOf<WorkingStep>()
        workedOn.forEach {
            it.tick {
                if (it.time == 0) {
                    freeWorkers++
                    finished.add(it)
                }
            }
        }
        workedOn.removeAll { finished.contains(it) }

        val dependsOnWork = workedOn.flatMap { it.allow }
        val dependsOnOthers = remaining.values.flatMap { it.allow }
        val nowAvailable = finished.flatMap { it.allow }
        available.addAll(remaining.keys + nowAvailable - dependsOnWork - dependsOnOthers)
        available.pop(freeWorkers).forEach {
            freeWorkers--
            workedOn.add(WorkingStep(it, remaining.remove(it)?.allow ?: emptySet()))
        }
    }
    return clock
}
