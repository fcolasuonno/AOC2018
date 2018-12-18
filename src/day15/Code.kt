package day15

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

data class Distance(val step: Int, val pos: Position, val thisPosition: Position)
data class Position(val x: Int, val y: Int) {
    fun ranges() = listOf(this.copy(y = y - 1),
            this.copy(x = x - 1),
            this.copy(x = x + 1),
            this.copy(y = y + 1))
}

sealed class Type
object Wall : Type()
open class Fighter(var position: Position, var hp: Int = 200, var attackPower: Int = 3, var killed: Boolean = false) : Type() {

    fun doTurn(enemy: TreeSet<out Fighter>, area: List<MutableList<Type>>): Boolean {
        if (enemy.isEmpty() || enemy.all { it.killed }) {
            return true
        }
        if (killed) {
            area[position.x][position.y] = Space
        } else {
            if (this.position.ranges().none { inRange -> enemy.any { !it.killed && it.position == inRange } }) {
                move(enemy, area)
            }
            val attack = enemy.filter { !it.killed && it.position in this.position.ranges() }.sortedWith(compareBy(
                    {
                        -it.hp
                    },
                    {
                        -it.position.y
                    },
                    {
                        -it.position.x
                    })).lastOrNull()
            attack?.let {
                it.hp -= attackPower
                if (it.hp <= 0) {
                    it.killed = true
                    area[it.position.x][it.position.y] = Space
                }
            }
        }
        return false
    }

    private fun move(enemy: TreeSet<out Fighter>, area: List<MutableList<Type>>) {
        val ranges = enemy.filter { !it.killed }.flatMap {
            it.position.ranges()
        }.filter { area[it.x][it.y] == Space }.sortedWith(compareBy(
                {
                    it.y
                },
                {
                    it.x
                }))
        val reachable = area.mapIndexed { x, line ->
            line.mapIndexed { y, t ->
                if (x == this.position.x && y == this.position.y) {
                    Distance(0, this.position, Position(x, y))
                } else if (area[x][y] != Space) {
                    Distance(-1, this.position, Position(x, y))
                } else {
                    null
                }
            }.toMutableList()
        }
        val positions = ArrayDeque<Pair<Position, Position>>()
        positions.addAll(position.ranges().map { it to position })
        while (positions.isNotEmpty()) {
            val next = positions.pop()
            val distance = next.second.let { reachable[it.x][it.y]!!.step } + 1
            if (reachable[next.first.x][next.first.y] == null) {
                reachable[next.first.x][next.first.y] = Distance(distance, next.second, next.first)
                positions.addAll(next.first.ranges().map { it to next.first })
            }
        }
        ranges.groupBy { reachable[it.x][it.y]?.step ?: Int.MAX_VALUE }.minBy { it.key }?.let {
            val final = it.value.minWith(compareBy(Position::y, Position::x))!!
            reachable[final.x][final.y]?.let {
                area[this.position.x][this.position.y] = Space
                var nextStep = it
                while (nextStep.pos != this.position) {
                    nextStep = reachable[nextStep.pos.x][nextStep.pos.y]!!
                }
                this.position = nextStep.thisPosition
                area[this.position.x][this.position.y] = this
            }
        }

    }
}


class Goblin(position: Position) : Fighter(position) {
    override fun toString() = "G($hp)"
}

class Elf(position: Position) : Fighter(position) {
    override fun toString() = "E($hp)"
}

object Space : Type()

fun parse(input: List<String>) = input.map {
    it.toCharArray().map { it }
}.requireNoNulls().let {
    Fight(List(it[0].size) { x ->
        List(it.size) { y ->
            it[y][x]
        }
    })
}

data class Fight(val input: List<List<Char>>, var round: Int = 0) {
    val area = input.map {
        it.toCharArray().map { c ->
            when (c) {
                '#' -> Wall
                'G' -> Goblin(Position(0, 0))
                'E' -> Elf(Position(0, 0))
                else -> Space
            }
        }.toMutableList()
    }
    val readingOrder = compareBy<Fighter>({ it.position.y }, { it.position.x })
    val elfs = sortedSetOf<Elf>(readingOrder)
    val goblins = sortedSetOf<Goblin>(readingOrder)
    val fighters = sortedSetOf(readingOrder)
    override fun toString() = buildString {
        append('\n')
        for (y in 0 until area[0].size) {
            val enemies = mutableListOf<Fighter>()
            for (x in 0 until area.size) {
                append(when (area[x][y]) {
                    Wall -> '#'
                    is Elf -> {
                        enemies.add(area[x][y] as Fighter)
                        'E'
                    }
                    is Goblin -> {
                        enemies.add(area[x][y] as Fighter)
                        'G'
                    }
                    Space -> '.'
                    else -> '?'
                })
            }
            append('\t')
            append(enemies.joinToString(", "))
            append('\n')
        }
    }

    fun fight() {
        while (true) {
            round++
            elfs.clear()
            goblins.clear()
            fighters.clear()
            for (x in 0 until area.size) {
                for (y in 0 until area[0].size) {
                    val type = area[x][y]
                    if (type is Elf) {
                        val positioned = type.apply { position = Position(x, y) }
                        elfs.add(positioned)
                        fighters.add(positioned)
                    }
                    if (type is Goblin) {
                        val positioned = type.apply { position = Position(x, y) }
                        goblins.add(positioned)
                        fighters.add(positioned)
                    }
                }
            }
            for (fighter in fighters) {
                if (fighter.doTurn(if (fighter is Elf) goblins else elfs, area)) {
                    println("Final $round round")
                    println(this)
                    return
                }
            }
            println("After $round round")
            println(this)
        }
    }

    fun outcome(): Int {
        println("${(round - 1)} * ${(fighters.filterNot { it.killed }.sumBy { it.hp })}")
        return (round - 1) * (fighters.filterNot { it.killed }.sumBy { it.hp })
    }

    fun anyElfKilled(elfAttack: Int): Boolean {
        area.forEach {
            it.filterIsInstance(Elf::class.java).forEach {
                it.attackPower = elfAttack
            }
        }
        while (elfs.none { it.killed }) {
            round++
            elfs.clear()
            goblins.clear()
            fighters.clear()
            for (x in 0 until area.size) {
                for (y in 0 until area[0].size) {
                    val type = area[x][y]
                    if (type is Elf) {
                        val positioned = type.apply { position = Position(x, y) }
                        elfs.add(positioned)
                        fighters.add(positioned)
                    }
                    if (type is Goblin) {
                        val positioned = type.apply { position = Position(x, y) }
                        goblins.add(positioned)
                        fighters.add(positioned)
                    }
                }
            }
            for (fighter in fighters) {
                if (fighter.doTurn(if (fighter is Elf) goblins else elfs, area)) {
                    println("Final $round round")
                    println(this)
                    return false
                }
            }
            println("After $round round")
            println(this)
        }
        return true
    }
}

fun part1(input: Fight): Int {
    val part1 = input.copy(input = input.input.map { it.map { it }.toMutableList() })
    part1.fight()
    return part1.outcome()
}

fun part2(input: Fight): Int {
    var part2 = input.copy()
    var elfAttack = 4
    while (part2.anyElfKilled(elfAttack++)) {
        part2 = input.copy()
    }
    return part2.outcome()
}

