package day4

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

data class LogEntries(
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val info: String
) {
    val date = "$month-$day"
    fun isGuardEntry() = info.startsWith("#")
}

@Suppress("ArrayInDataClass")
data class Day(
    val date: String,
    val guard: Int,
    val logs: List<LogEntries>,
    var sleep: Array<Int> = Array(60) { 0 }
) {

    init {
        logs.windowed(size = 2, step = 2).forEach {
            for (i in it[0].minute until it[1].minute) {
                sleep[i] = 1
            }
        }
    }
}

private val lineStructure = """\[1518-(\d+)-(\d+) (\d+):(\d+)] [^ ]+ ([^ ]+).+""".toRegex()

typealias Guard = Int

fun parse(input: List<String>): Map<Guard, List<Day>> {
    val logEntries = input.sorted().map {
        lineStructure.matchEntire(it)?.destructured?.let {
            val (M, d, h, m, action) = it
            LogEntries(M.toInt(), d.toInt(), h.toInt(), m.toInt(), action)
        }
    }.requireNoNulls()

    val days = mutableListOf<List<LogEntries>>()
    var dayLog = mutableListOf<LogEntries>()
    for (log in logEntries) {
        if (log.isGuardEntry()) {
            days.add(dayLog)
            dayLog = mutableListOf()
        }
        dayLog.add(log)
    }
    days.add(dayLog)

    return days.drop(1).filter {
        it.size > 1
    }.map {
        Day(
            it[1].date,
            it.first().info.drop(1).toInt(),
            it.drop(1)
        )
    }.groupBy(Day::guard)
}

fun part1(input: Map<Guard, List<Day>>) = input.map {
    val sleptMinutes = Array(60) { i ->
        it.value.sumBy {
            it.sleep[i]
        }
    }
    val mostSleptMinute = requireNotNull(sleptMinutes.indices.maxBy {
        sleptMinutes[it]
    })
    val totalSleep = it.value.sumBy { it.sleep.sum() }
    Triple(it.key, totalSleep, mostSleptMinute)
}.maxBy {
    it.second
}?.let {
    it.first * it.third
}

fun part2(input: Map<Int, List<Day>>) = input.map {
    val sleptMinutes = Array(60) { i ->
        it.value.sumBy {
            it.sleep[i]
        }
    }
    val mostSleptMinute = requireNotNull(sleptMinutes.indices.maxBy {
        sleptMinutes[it]
    })
    Triple(it.key, sleptMinutes[mostSleptMinute], mostSleptMinute)
}.maxBy {
    it.second
}?.let {
    it.first * it.third
}
