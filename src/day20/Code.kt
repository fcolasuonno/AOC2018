package day20

import java.io.File
import java.util.*
import kotlin.math.min

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

data class Location(val x: Int, var y: Int)

sealed class Step(open val previous: Step?, open val location: Location)
data class Root(override val location: Location = Location(0, 0)) : Step(null, location) {
    override fun toString() = "^"
}

data class SimpleStep(override val previous: Step, val direction: Char) : Step(previous, location = previous.location.run {
    when (direction) {
        'N' -> copy(y = y - 1)
        'S' -> copy(y = y + 1)
        'W' -> copy(x = x - 1)
        'E' -> copy(x = x + 1)
        else -> throw IllegalStateException()
    }
}) {
    override fun toString() = direction.toString()
}

data class Branch(override val previous: Step, val branches: MutableList<MutableList<Step>> = mutableListOf()) : Step(previous, previous.location) {
    override fun toString() = branches.toString()
}

fun parse(input: List<String>) = input.map {
    val currentStep = ArrayDeque<MutableList<Step>>()
    currentStep.push(mutableListOf(Root()))
    it.forEach {
        val previous = currentStep.peek().last()
        when (it) {
            'N' -> {
                currentStep.peek().add(SimpleStep(previous, 'N'))
            }
            'W' -> {
                currentStep.peek().add(SimpleStep(previous, 'W'))
            }
            'S' -> {
                currentStep.peek().add(SimpleStep(previous, 'S'))
            }
            'E' -> {
                currentStep.peek().add(SimpleStep(previous, 'E'))
            }
            '(' -> {
                currentStep.peek().add(Branch(previous))
                currentStep.push(mutableListOf(Root(previous.location)))
            }
            '|' -> {
                val branch = currentStep.pop()
                (currentStep.peek().last() as Branch).branches.add(branch)
                currentStep.push(mutableListOf(Root((currentStep.peek().last() as Branch).location)))
            }
            ')' -> {
                val branch = currentStep.pop()
                (currentStep.peek().last() as Branch).branches.add(branch)
            }
            else -> Unit
        }
    }
    currentStep.single()
}.requireNoNulls()


fun addDistances(distanceMap: MutableMap<Location, Int>, steps: MutableList<Step>): Unit = steps.forEach {
    when (it) {
        is SimpleStep -> distanceMap[it.location] = min(
                distanceMap.getValue(it.previous.location) + 1,
                distanceMap[it.location] ?: Int.MAX_VALUE
        )
        is Branch -> it.branches.forEach { addDistances(distanceMap, it) }
    }
}

fun part1(input: List<MutableList<Step>>) = input.map {
    mutableMapOf(Location(0, 0) to 0).apply { addDistances(this, it) }.values.max()
}

fun part2(input: List<MutableList<Step>>) = input.map {
    mutableMapOf(Location(0, 0) to 0).apply { addDistances(this, it) }.count { it.value >= 1000 }
}
