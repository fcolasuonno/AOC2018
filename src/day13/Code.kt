package day13

import java.io.File
import java.util.*

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

fun parse(input: List<String>) = input.map {
    it.toList()
}.let {
    Track(it)
}

sealed class TrackElement(var type: Char) {
    open fun tick(track: Track) = Unit
}

open class Rail(type: Char) : TrackElement(type)

class Intersection(type: Char) : Rail(type)
class Train(type: Char, val id: Int, var x: Int, var y: Int, var rail: Rail, var crashed: Boolean = false) :
    TrackElement(type) {
    enum class Direction { LEFT, RIGHT, STRAIGHT }

    var nextTurn = Direction.RIGHT
    fun turn(where: Direction) = when (where) {
        Direction.LEFT -> when (type) {
            '>' -> '^'
            '<' -> 'v'
            '^' -> '<'
            'v' -> '>'
            else -> throw InputMismatchException()
        }
        Direction.RIGHT -> when (type) {
            '>' -> 'v'
            '<' -> '^'
            '^' -> '>'
            'v' -> '<'
            else -> throw InputMismatchException()
        }
        Direction.STRAIGHT -> type
    }

    override fun tick(track: Track) {
        if (crashed) return
        track.trackData[x][y] = rail
        when (type) {
            '>' -> x++
            '<' -> x--
            '^' -> y--
            'v' -> y++
        }
        val nextElement = follow(track.trackData[x][y])
        track.trackData[x][y] = nextElement
        if (nextElement is Crash) {
            track.crashed = true
            track.crashes.add(nextElement)
            track.trackData[x][y] = nextElement.train2.rail
            track.trains[nextElement.train1.id]!!.crashed = true
            track.trains[nextElement.train2.id]!!.crashed = true
            track.trains.remove(nextElement.train1.id)
            track.trains.remove(nextElement.train2.id)
        }
    }

    fun follow(rail: TrackElement): TrackElement {
        when (rail) {
            is Rail -> {
                this.rail = rail
                type = when (rail.type) {
                    '/' -> when (type) {
                        '>' -> '^'
                        '<' -> 'v'
                        '^' -> '>'
                        'v' -> '<'
                        else -> throw InputMismatchException()
                    }
                    '\\' -> when (type) {
                        '>' -> 'v'
                        '<' -> '^'
                        '^' -> '<'
                        'v' -> '>'
                        else -> throw InputMismatchException()
                    }
                    '+' -> {
                        updateTurn()
                        turn(nextTurn)
                    }
                    else -> type
                }
            }
            is Train -> {
                return Crash('x', this, rail)
            }
        }

        return this
    }

    private fun updateTurn() {
        when (nextTurn) {
            Direction.LEFT -> nextTurn = Direction.STRAIGHT
            Direction.STRAIGHT -> nextTurn = Direction.RIGHT
            Direction.RIGHT -> nextTurn = Direction.LEFT
        }
    }
}

class Space(type: Char) : TrackElement(type)
class Crash(type: Char, val train1: Train, val train2: Train) : Rail(type)

class Track(
    input: List<List<Char>>,
    val crashes: MutableList<Crash> = mutableListOf(),
    val trains: MutableMap<Int, Train> = mutableMapOf()
) {
    val sizeY = input.size
    val sizeX = input.map { it.size }.max()!!
    var trainid = 0
    val trackData = MutableList(sizeX) { x ->
        MutableList(sizeY) { y ->
            val c = input[y].getOrElse(x) { ' ' }
            when (c) {
                '/', '-', '|', '\\' -> Rail(c)
                '+' -> Intersection(c)
                '>', '^', '<', 'v' -> Train(
                    c, trainid++, x, y, when (c) {
                        '>' -> Rail('-')
                        '<' -> Rail('-')
                        '^' -> Rail('|')
                        'v' -> Rail('|')
                        else -> throw InputMismatchException()
                    }
                ).also {
                    trains.put(it.id, it)
                }
                else -> Space(c)
            }
        }
    }
    var crashed = false

    fun print() {
        for (j in 0 until sizeY) {
            for (i in 0 until sizeX) {
                print(trackData[i][j].type)
            }
            println()
        }
    }

    fun tick() {
        trains.values.sortedBy {
            it.x + it.y * sizeX
        }.forEach {
            it.tick(this)
        }
    }
}

fun part1(input: Track): Pair<Int, Int> {
    while (!input.crashed) {
//        input.print()
        input.tick()
    }
    return input.crashes.first().train1.let { Pair(it.x, it.y) }
}

fun part2(input: Track): Pair<Int, Int> {
    while (input.trains.size > 1) {
        input.tick()
//        input.print()
    }
    return input.trains.values.single().let { Pair(it.x, it.y) }
}