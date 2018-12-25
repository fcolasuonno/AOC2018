package day22

import java.io.File
import java.util.*
import kotlin.math.max

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

data class Position(val x: Int, val y: Int) {
    fun neighbours() = listOf(copy(x = x - 1), copy(x = x + 1), copy(y = y - 1), copy(y = y + 1))
}

private val lineStructure1 = """depth: (\d+)""".toRegex()
private val lineStructure2 = """target: (\d+),(\d+)""".toRegex()

fun parse(input: List<String>) = Cave(
        lineStructure1.matchEntire(input[0])!!.groupValues[1].toInt(),
        lineStructure2.matchEntire(input[1])!!.groupValues.let { Position(it[1].toInt(), it[2].toInt()) }
)

class Cave(private val depth: Int, val target: Position) {
    private var lastAvailablePosition = Position(0, 0)
    private val cave = mutableMapOf(lastAvailablePosition to calcLevel(0, 0))

    init {
        get(target)
    }

    data class Region(val geologic: Int, val erosion: Int) {
        companion object {
            fun build(geologic: Int, depth: Int) = Region(geologic, (geologic + depth) % 20183)
        }
    }

    private fun calcLevel(x: Int, y: Int): Region {
        if (x == 0 && y == 0) return Region.build(0, depth)
        if (x == target.x && y == target.y) return Region.build(0, depth)
        if (y == 0) return Region.build(x * 16807, depth)
        if (x == 0) return Region.build(y * 48271, depth)
        return Region.build(cave.getValue(Position(x - 1, y)).erosion * cave.getValue(Position(x, y - 1)).erosion, depth)
    }

    operator fun get(position: Position): Type {
        val newLastAvailable = Position(max(lastAvailablePosition.x, position.x), max(lastAvailablePosition.y, position.y))

        for (x in (lastAvailablePosition.x + 1)..newLastAvailable.x) {
            for (y in 0..lastAvailablePosition.y) {
                cave[Position(x, y)] = calcLevel(x, y)
            }
        }
        for (y in (lastAvailablePosition.y + 1)..newLastAvailable.y) {
            for (x in 0..newLastAvailable.x) {
                cave[Position(x, y)] = calcLevel(x, y)
            }
        }
        lastAvailablePosition = newLastAvailable
        return when (cave.getValue(position).erosion % 3) {
            0 -> Type.ROCK
            1 -> Type.WET
            else -> Type.NARROW
        }
    }
}

enum class Type { ROCK, WET, NARROW;

    fun changeTool(tool: Tool) = when (this) {
        Type.ROCK -> if (tool == Tool.TORCH) Tool.CLIMB else Tool.TORCH
        Type.WET -> if (tool == Tool.CLIMB) Tool.NEITHER else Tool.CLIMB
        Type.NARROW -> if (tool == Tool.NEITHER) Tool.TORCH else Tool.NEITHER
    }

    fun supports(tool: Tool) = when (this) {
        Type.ROCK -> (tool == Tool.TORCH || tool == Tool.CLIMB)
        Type.WET -> (tool == Tool.CLIMB || tool == Tool.NEITHER)
        Type.NARROW -> (tool == Tool.NEITHER || tool == Tool.TORCH)
    }
}

enum class Tool { TORCH, CLIMB, NEITHER }

fun part1(input: Cave) = (0..input.target.x).sumBy { x ->
    (0..input.target.y).sumBy { y ->
        when (input[Position(x, y)]) {
            Type.ROCK -> 0
            Type.WET -> 1
            Type.NARROW -> 2
        }
    }
}

data class UpdateInfo(val min: Int, val pos: Position, val tool: Tool) : Comparable<UpdateInfo> {
    override fun compareTo(other: UpdateInfo) = compareValuesBy(this, other, { it.min }, { it.pos.y }, { it.pos.x }, { it.tool.ordinal })

    fun neighbours(input: Cave) = pos.neighbours()
            .filter { it.x >= 0 && it.y >= 0 && input[it].supports(tool) }
            .map { copy(min = min + 1, pos = it) }
            .plus(copy(min = min + 7, tool = input[pos].changeTool(tool)))
}

fun part2(input: Cave): Int {
    var current = UpdateInfo(0, Position(0, 0), Tool.TORCH)
    val frontier = PriorityQueue<UpdateInfo>()
    frontier.add(current)
    val distance = mutableMapOf(Position(0, 0) to Tool.TORCH to 0)
    while (frontier.isNotEmpty()) {
        current = frontier.poll()
        if (current.pos == input.target && current.tool == Tool.TORCH) {
            break
        }
        if (distance.getValue(current.pos to current.tool) < current.min) continue
        for (next in current.neighbours(input)) {
            if (next.min < distance[next.pos to next.tool] ?: Int.MAX_VALUE) {
                distance[next.pos to next.tool] = next.min
                frontier.add(next)
            }
        }
    }
    return current.min
}