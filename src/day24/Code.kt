package day24

import java.io.File

const val debugPrint = false
fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

data class Attributes(val map: Map<String, List<String>>) {
    fun modifier(damageType: String) = when {
        damageType in (map.getOrDefault("weak", emptyList())) -> 2
        damageType in (map.getOrDefault("immune", emptyList())) -> 0
        else -> 1
    }
}

data class Army(val infection: Boolean, var units: Int, val hitPoints: Int, val attributes: Attributes, val damage: Int, val damageType: String, val initiative: Int) {
    var dead = false
    fun effectivePower() = units * damage
    fun damageFrom(other: Army) = attributes.modifier(other.damageType) * other.effectivePower()

    companion object {
        var counterImmune = 1
        var counterInfection = 1
    }

    private val num = if (infection) counterInfection++ else counterImmune++
    override fun toString() = if (dead) "DEAD GROUP $num" else "${if (infection) "Infection" else "Immune"} group $num"
    fun defend(attacking: Army): Int {
        if (attacking.dead) return 0
        val receivedDamage = damageFrom(attacking)
        val kills = minOf(units, receivedDamage / hitPoints)
        units -= kills
        if (units == 0) {
            dead = true
        }
        return kills
    }
}

data class Game(var immune: List<Army>, var infection: List<Army>) {
    private fun otherArmy(army: List<Army>) = if (army.any { it.infection }) immune else infection
    fun gamePlanFor(army: List<Army>): List<Pair<Army, Army?>> {
        val attacked = mutableSetOf<Army>()
        val other = otherArmy(army).filterNot { it.dead }
        val attackPlan = army
                .filterNot { it.dead }
                .toSortedSet(compareBy({ it.effectivePower() }, { it.initiative }))
                .reversed()
                .map { attacking ->
                    if (debugPrint) println("$attacking considering ${other.filter { it.damageFrom(attacking) > 0 && !attacked.contains(it) }}")
                    attacking to other.filter { it.damageFrom(attacking) > 0 && !attacked.contains(it) }.onEach {
                        if (debugPrint) println("$attacking would deal ${it.damageFrom(attacking)} to $it")
                    }.maxWith(compareBy({ it.damageFrom(attacking) }, { it.effectivePower() }, { it.initiative }))?.also {
                        attacked.add(it)
                    }
                }
        return attackPlan
    }

    fun finished() = immune.all { it.dead } || infection.all { it.dead }
    private fun clean() {
        immune = immune.filterNot { it.dead }
        infection = infection.filterNot { it.dead }
    }

    fun nextTurn(): Boolean {
        val infectionPlan = gamePlanFor(infection)
        val immunePlan = gamePlanFor(immune)
        if (debugPrint) {
            println(immunePlan)
            println(infectionPlan)
        }
        var actionTaken = false
        (immunePlan + infectionPlan)
                .toMap()
                .toSortedMap(compareByDescending { it.initiative })
                .forEach {
                    val executionResult = it.value?.defend(it.key)
                    if (executionResult != null && executionResult > 0) {
                        actionTaken = true
                    }
                    if (debugPrint) println("${it.key} attacks defending group ${it.value}, killing $executionResult")
                    if (finished()) {
                        return true
                    }
                }
        clean()
        return actionTaken
    }
}

private val lineStructure = """(\d+) units each with (\d+) hit points (\([^)]+\) )?with an attack that does (\d+) ([^)]+) damage at initiative (\d+)""".toRegex()
private val attributeStructure = """([^ ]+) to (.+)""".toRegex()

fun parse(input: List<String>) = Game(input.drop(1).takeWhile { it.matches(lineStructure) }.map { parseArmy(false, it) }, input.takeLastWhile { it.matches(lineStructure) }.map { parseArmy(true, it) })

private fun parseArmy(infection: Boolean, inputLine: String) = lineStructure.matchEntire(inputLine)!!.groupValues.let {
    val attributes = it[3].takeIf { it.isNotBlank() }?.let {
        it.drop(1).dropLast(2).split("; ".toRegex()).map {
            attributeStructure.matchEntire(it)!!.destructured.let {
                val (attributeType, attributeValues) = it
                attributeType to attributeValues.split(", ".toRegex())
            }
        }.associate { it.first to it.second }
    } ?: emptyMap()
    Army(infection, it[1].toInt(), it[2].toInt(), Attributes(attributes), it[4].toInt(), it[5], it[6].toInt())
}

fun part1(originalInput: Game): Int {
    val input = Game(originalInput.immune.map { it.copy() }, originalInput.infection.map { it.copy() })
    while (!input.finished()) {
        val infectionPlan = input.gamePlanFor(input.infection)
        val immunePlan = input.gamePlanFor(input.immune)
        if (debugPrint) {
            println(immunePlan)
            println(infectionPlan)
        }
        (immunePlan + infectionPlan)
                .toMap()
                .toSortedMap(compareByDescending { it.initiative })
                .forEach {
                    val executionResult = it.value?.defend(it.key)
                    if (debugPrint) println("${it.key} attacks defending group ${it.value}, killing $executionResult")
                    if (input.finished()) {
                        return@forEach
                    }
                }
    }
    val immuneResult = input.immune.filterNot { it.dead }.map { it.units }.sum()
    val infectionResult = input.infection.filterNot { it.dead }.map { it.units }.sum()
    if (minOf(immuneResult, infectionResult) != 0) throw IllegalStateException()
    return maxOf(immuneResult, infectionResult)
}

fun part2(originalInput: Game): Int {
    var boost = 1
    var input: Game
    var immuneResult: Int
    var infectionResult: Int
    do {
        input = Game(originalInput.immune.map { it.copy(damage = it.damage + boost) }, originalInput.infection.map { it.copy() })
        while (!input.finished()) {
            if (!input.nextTurn()) {
                break
            }
        }
        immuneResult = input.immune.filterNot { it.dead }.map { it.units }.sum()
        infectionResult = input.infection.filterNot { it.dead }.map { it.units }.sum()
        boost++
    } while (infectionResult != 0)
    return immuneResult
}