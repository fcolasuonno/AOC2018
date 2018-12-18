package day16

import java.io.File

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

inline fun regreg(crossinline f: (Int, Int) -> Int) = { mem: Mem, op: Op ->
    mem.mapIndexed { i: Int, v: Int -> if (i == op[3]) f(mem[op[1]], mem[op[2]]) else v }
}

inline fun regi(crossinline f: (Int, Int) -> Int) = { mem: Mem, op: Op ->
    mem.mapIndexed { i: Int, v: Int -> if (i == op[3]) f(mem[op[1]], op[2]) else v }
}

inline fun ireg(crossinline f: (Int, Int) -> Int) = { mem: Mem, op: Op ->
    mem.mapIndexed { i: Int, v: Int -> if (i == op[3]) f(op[1], mem[op[2]]) else v }
}

val opcodes = mapOf(
        "addr" to regreg { a, b -> a + b },
        "addi" to regi { a, b -> a + b },
        "mulr" to regreg { a, b -> a * b },
        "muli" to regi { a, b -> a * b },
        "banr" to regreg { a, b -> a and b },
        "bani" to regi { a, b -> a and b },
        "borr" to regreg { a, b -> a or b },
        "bori" to regi { a, b -> a or b },
        "setr" to regreg { a, b -> a },
        "seti" to ireg { a, b -> a },
        "gtir" to ireg { a, b -> if (a > b) 1 else 0 },
        "gtri" to regi { a, b -> if (a > b) 1 else 0 },
        "gtrr" to regreg { a, b -> if (a > b) 1 else 0 },
        "eqir" to ireg { a, b -> if (a == b) 1 else 0 },
        "eqri" to regi { a, b -> if (a == b) 1 else 0 },
        "eqrr" to regreg { a, b -> if (a == b) 1 else 0 }
)

private val lineStructure1 = """Before: \[(\d+), (\d+), (\d+), (\d+)]""".toRegex()
private val lineStructure2 = """(\d+) (\d+) (\d+) (\d+)""".toRegex()
private val lineStructure3 = """After:  \[(\d+), (\d+), (\d+), (\d+)]""".toRegex()

fun parse(input: List<String>) = Pair(
        input.dropLastWhile { lineStructure2.matches(it) }.chunked(4).filter { it.size == 4 }.map {
            val before = lineStructure1.matchEntire(it[0])?.destructured?.let { it.toList().map { it.toInt() } }!!
            val op = lineStructure2.matchEntire(it[1])?.destructured?.let { it.toList().map { it.toInt() } }!!
            val after = lineStructure3.matchEntire(it[2])?.destructured?.let { it.toList().map { it.toInt() } }!!
            Test(before, op, after)
        }.requireNoNulls(),
        input.takeLastWhile { lineStructure2.matches(it) }.map {
            lineStructure2.matchEntire(it)?.destructured?.let { it.toList().map { it.toInt() } }!!
        }.requireNoNulls())

typealias Mem = List<Int>
typealias Op = List<Int>

data class Test(val before: Mem, val op: Op, val after: Mem)

fun part1(parsed: Pair<List<Test>, List<Op>>) = parsed.first.count { t ->
    opcodes.count { it.value(t.before, t.op) == t.after } >= 3
}

fun part2(parsed: Pair<List<Test>, List<Op>>): Any {
    val opMap = parsed.first.groupBy { it.op[0] }.mapValues {
        opcodes.filter { f ->
            it.value.all { f.value(it.before, it.op) == it.after }
        }.map { it.key }
    }.toMutableMap()
    while (opMap.any { it.value.size != 1 }) {
        val resolved = opMap.filter { it.value.size == 1 }.flatMap { it.value }
        opMap.forEach { t, u ->
            if (u.size != 1) {
                opMap[t] = u - resolved
            }
        }
    }
    val translatedMap = opMap.mapValues {
        opcodes.getValue(it.value.single())
    }
    var mem = listOf(0, 0, 0, 0)
    parsed.second.forEach {
        mem = (translatedMap.getValue(it[0]))(mem, it)
    }
    return mem[0]
}
