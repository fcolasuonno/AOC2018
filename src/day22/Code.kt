package day22

import printWith
import java.io.File

fun main(args: Array<String>) {
    val name = if (true) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    //println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

data class Position(val x: Int, val y: Int)

private val lineStructure1 = """depth: (\d+)""".toRegex()
private val lineStructure2 = """target: (\d+),(\d+)""".toRegex()

fun parse(input: List<String>) = Pair(input[0].let {
    lineStructure1.matchEntire(it)?.destructured?.let {
        val (depth) = it
        depth.toInt()
    }
}!!, input[1].let {
    lineStructure2.matchEntire(it)?.destructured?.let {
        val (x, y) = it
        Position(x.toInt(), y.toInt())
    }
}!!).let { input ->
    val cave = List(input.second.x * 2) {
        MutableList(input.second.y * 2) { Region(0, 0) }
    }
    for (x in 0..input.second.x) {
        for (y in 0..input.second.y) {
            cave[x][y] = calcLevel(x, y, cave, input.first, input.second)
        }
    }
    Pair(List(cave.size) { x ->
        List(cave.first().size) { y ->
            when (cave[x][y].erotion % 3) {
                0 -> Type.ROCK
                1 -> Type.WET
                else -> Type.NARROW
            }
        }
    }, input.second)
}

enum class Type { ROCK, WET, NARROW }
enum class Tool { TORCH, CLIMB, NEITHER }

data class Region(val geologic: Int, val erotion: Int) {
    companion object {
        fun build(geologic: Int, depth: Int) = Region(geologic, (geologic + depth) % 20183)
    }
}

fun calcLevel(x: Int, y: Int, cave: List<MutableList<Region>>, depth: Int, target: Position): Region {
    if (x == 0 && y == 0) return Region.build(0, depth)
    if (x == target.x && y == target.y) return Region.build(0, depth)
    if (y == 0) return Region.build(x * 16807, depth)
    if (x == 0) return Region.build(y * 48271, depth)
    return Region.build(cave[x - 1][y].erotion * cave[x][y - 1].erotion, depth)
}

fun part1(input: Pair<List<List<Type>>, Position>) = input.first.filterIndexed { i, v -> i < input.second.x }.sumBy {
    it.filterIndexed { i, v -> i < input.second.y }.sumBy {
        when (it) {
            Type.ROCK -> 0
            Type.WET -> 1
            Type.NARROW -> 2
        }
    }
}

data class Cost(val type: Type) {
    operator fun get(tool: Tool) =
            distanceMap[tool] ?: Int.MAX_VALUE

    operator fun set(tool: Tool, value: Int) {
        distanceMap[tool] = value
    }

    fun changeTool(tool: Tool) = when (type) {
        Type.ROCK -> if (tool == Tool.TORCH) Tool.CLIMB else Tool.TORCH
        Type.WET -> if (tool == Tool.CLIMB) Tool.NEITHER else Tool.CLIMB
        Type.NARROW -> if (tool == Tool.NEITHER) Tool.TORCH else Tool.NEITHER
    }

    fun supports(tool: Tool) = when (type) {
        Type.ROCK -> (tool == Tool.TORCH || tool == Tool.CLIMB)
        Type.WET -> (tool == Tool.CLIMB || tool == Tool.NEITHER)
        Type.NARROW -> (tool == Tool.NEITHER || tool == Tool.TORCH)
    }

    val distanceMap = mutableMapOf<Tool, Int>()
}

fun part2(input: Pair<List<List<Type>>, Position>): Int {
    val distance = List(input.first.size) { x ->
        MutableList(input.first.first().size) { y ->
            Cost(input.first[x][y])
        }
    }
    val updat = mutableSetOf<Triple<Position, Tool, Int>>()
    updat.addAll(update(distance, Position(0, 0), Tool.TORCH, 0))
    while (updat.isNotEmpty()) {
        val new = updat.iterator().next()
        updat.remove(new)
        updat.addAll(update(distance, new.first, new.second, new.third))
    }
    println(distance.printWith {
        when (it.type) {
            Type.ROCK -> "."
            Type.NARROW -> "|"
            Type.WET -> "="
        }
    })
    return distance[input.second.x][input.second.y][Tool.TORCH]
}

fun update(distance: List<List<Cost>>, pos: Position, tool: Tool, value: Int): MutableSet<Triple<Position, Tool, Int>> {
    val toUpdate = mutableSetOf<Triple<Position, Tool, Int>>()
    if ((pos.x in 0 until distance.size) && pos.y in 0 until distance.first().size && distance[pos.x][pos.y].supports(tool)) {
        val cost = distance[pos.x][pos.y]
        if (value < (cost[tool])) {
            cost[tool] = value
            toUpdate.add(Triple(Position(pos.x + 1, pos.y), tool, value + 1))
            toUpdate.add(Triple(Position(pos.x - 1, pos.y), tool, value + 1))
            toUpdate.add(Triple(Position(pos.x, pos.y + 1), tool, value + 1))
            toUpdate.add(Triple(Position(pos.x, pos.y - 1), tool, value + 1))
        }
        val otherTool = cost.changeTool(tool)
        if ((value + 7) < (cost[otherTool])) {
            cost[otherTool] = value + 7
            toUpdate.add(Triple(Position(pos.x + 1, pos.y), otherTool, value + 8))
            toUpdate.add(Triple(Position(pos.x - 1, pos.y), otherTool, value + 8))
            toUpdate.add(Triple(Position(pos.x, pos.y + 1), otherTool, value + 8))
            toUpdate.add(Triple(Position(pos.x, pos.y - 1), otherTool, value + 8))
        }
    }
    return toUpdate
}
