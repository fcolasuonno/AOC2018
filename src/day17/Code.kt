package day17

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

data class Clay(val xRange: IntRange, val yRange: IntRange)

private val lineStructure1 = """x=(\d+)(?:\.\.)?(\d+)?, y=(\d+)(?:\.\.)?(\d+)?""".toRegex()
private val lineStructure2 = """y=(\d+)(?:\.\.)?(\d+)?, x=(\d+)(?:\.\.)?(\d+)?""".toRegex()

fun parse(input: List<String>) = input.map {
    lineStructure1.matchEntire(it)?.destructured?.let {
        val (xFrom, xTo, yFrom, yTo) = it.toList()
        val x = xFrom.toInt()
        val y = yFrom.toInt()
        Clay(x..(if (xTo.isNotEmpty()) xTo.toInt() else x), y..(if (yTo.isNotEmpty()) yTo.toInt() else y))
    } ?: lineStructure2.matchEntire(it)?.destructured?.let {
        val (yFrom, yTo, xFrom, xTo) = it.toList()
        val x = xFrom.toInt()
        val y = yFrom.toInt()
        Clay(x..(if (xTo.isNotEmpty()) xTo.toInt() else x), y..(if (yTo.isNotEmpty()) yTo.toInt() else y))
    }
}.requireNoNulls().let {
    val minX = it.map { it.xRange.start }.min()
    val maxX = it.map { it.xRange.endInclusive }.max()
    val minY = it.map { it.yRange.start }.min()
    val maxY = it.map { it.yRange.endInclusive }.max()
    Ground(minX!!, minY!!, maxX!!, maxY!!, it).also {
        it.fill()
    }
}

enum class Type {
    CLAY,
    SAND,
    WATER_FLOW,
    WATER_REST;

    fun isWater() = (this == Type.WATER_REST || this == Type.WATER_FLOW)
    fun isFoundation() = (this == Type.CLAY || this == Type.WATER_REST)
}

class Ground(val minX: Int, val minY: Int, val maxX: Int, val maxY: Int, val clay: List<Clay>) {
    val map = MutableList(maxX + 2) { x ->
        MutableList(maxY + 2) { y ->
            Type.SAND
        }
    }.apply {
        clay.forEach { clay ->
            for (x in clay.xRange) {
                for (y in clay.yRange) {
                    this[x][y] = Type.CLAY
                }
            }
        }
        this[500][0] = Type.WATER_FLOW
    }

    data class Point(val x: Int, val y: Int)

    val sources = mutableListOf(Point(500, 0))

    override fun toString() = buildString {
        append('\n')
        for (y in 0 until map[0].size) {
            append("%5d ".format(y))
            for (x in (minX - 1) until map.size) {
                append(when (map[x][y]) {
                    Type.CLAY -> '#'
                    Type.SAND -> '.'
                    Type.WATER_FLOW -> '|'
                    Type.WATER_REST -> '~'
                })
            }
            append('\n')
        }
    }

    fun fill() {
        var modified = true
        while (modified) {
            modified = false
            val newSources = mutableListOf<Point>()
            sources.forEach {
                var s = it
                while (s.y <= maxY && map[s.x][s.y + 1] == Type.SAND) {
                    modified = true
                    map[s.x][s.y + 1] = Type.WATER_FLOW
                    s = s.copy(y = s.y + 1)
                }
                while (s.y <= maxY && map[s.x][s.y + 1] == Type.WATER_FLOW) {
                    s = s.copy(y = s.y + 1)
                }

                val fill = mutableSetOf<Point>()
                var r = s.copy(s.x + 1, s.y)
                var l = s.copy(s.x - 1, s.y)
                fill.add(s.copy())
                while (r.y <= maxY && map[r.x][r.y] == Type.SAND && map[r.x - 1][r.y + 1].isFoundation()) {
                    fill.add(r.copy())
                    modified = true
                    map[r.x][r.y] = Type.WATER_FLOW
                    r = r.copy(x = r.x + 1)
                }
                while (l.y <= maxY && map[l.x][l.y] == Type.SAND && map[l.x + 1][l.y + 1].isFoundation()) {
                    fill.add(l.copy())
                    modified = true
                    map[l.x][l.y] = Type.WATER_FLOW
                    l = l.copy(x = l.x - 1)
                }
                val flowing = mutableSetOf<Point>()
                flowing.addAll(fill)
                while (r.y <= maxY && map[r.x][r.y] == Type.WATER_FLOW) {
                    flowing.add(r.copy())
                    r = r.copy(x = r.x + 1)
                }
                while (l.y <= maxY && map[l.x][l.y] == Type.WATER_FLOW) {
                    flowing.add(l.copy())
                    l = l.copy(x = l.x - 1)
                }
                if (s.y <= maxY && flowing.all { map[it.x][it.y + 1].isFoundation() } &&
                        flowing.minBy { it.x }!!.let { map[it.x - 1][it.y] } == Type.CLAY &&
                        flowing.maxBy { it.x }!!.let { map[it.x + 1][it.y] } == Type.CLAY) {
                    flowing.forEach {
                        modified = true
                        map[it.x][it.y] = Type.WATER_REST
                    }
                }
                if (s.y <= maxY && fill.minBy { it.x }!!.let { map[it.x][it.y + 1] } == Type.SAND) {
                    newSources.add(fill.minBy { it.x }!!)
                }
                if (s.y <= maxY && fill.maxBy { it.x }!!.let { map[it.x][it.y + 1] } == Type.SAND) {
                    newSources.add(fill.maxBy { it.x }!!)
                }
            }
            sources.addAll(newSources)
        }
    }

    fun allWater() = map.sumBy { it.filterIndexed { index, type -> index in minY..maxY && type.isWater() }.count() }

    fun restWater() = map.sumBy { it.filterIndexed { index, type -> index in minY..maxY && (type == Type.WATER_REST) }.count() }
}

fun part1(input: Ground) = input.allWater()
fun part2(input: Ground) = input.restWater()
