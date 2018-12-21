package day21

import java.io.File

private var debugPrint = false

fun main(args: Array<String>) {
    val name = if (false) "test.txt" else "input.txt"
    val dir = ::main::class.java.`package`.name
    val input = File("src/$dir/$name").readLines()
    val parsed = parse(input)
    println("Part 1 = ${part1(parsed)}")
    println("Part 2 = ${part2(parsed)}")
}

inline fun regreg(crossinline f: (Int, Int) -> Int) = { mem: Mem, op: Op ->
    mem[op.dest] = f(mem.getOrElse(op.a) { 0 }, mem.getOrElse(op.b) { 0 })
}

inline fun regi(crossinline f: (Int, Int) -> Int) = { mem: Mem, op: Op ->
    mem[op.dest] = f(mem.getOrElse(op.a) { 0 }, op.b)
}

inline fun ireg(crossinline f: (Int, Int) -> Int) = { mem: Mem, op: Op ->
    mem[op.dest] = f(op.a, mem.getOrElse(op.b) { 0 })
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
        "setr" to regreg { a, _ -> a },
        "seti" to ireg { a, _ -> a },
        "gtir" to ireg { a, b -> if (a > b) 1 else 0 },
        "gtri" to regi { a, b -> if (a > b) 1 else 0 },
        "gtrr" to regreg { a, b -> if (a > b) 1 else 0 },
        "eqir" to ireg { a, b -> if (a == b) 1 else 0 },
        "eqri" to regi { a, b -> if (a == b) 1 else 0 },
        "eqrr" to regreg { a, b -> if (a == b) 1 else 0 }
)

private val lineStructure1 = """#ip (\d+)""".toRegex()
private val lineStructure2 = """([a-z]+) (\d+) (\d+) (\d+)""".toRegex()

fun parse(input: List<String>) = Pair(
        lineStructure1.matchEntire(input.first())?.destructured?.component1()?.toInt()!!,
        input.drop(1).map {
            lineStructure2.matchEntire(it)?.destructured?.let {
                val (opcode, a, b, dest) = it
                Op(opcode, a.toInt(), b.toInt(), dest.toInt())
            }
        }.requireNoNulls())

typealias Mem = IntArray

data class Op(val opCode: String, val a: Int, val b: Int, val dest: Int)

fun part1(parsed: Pair<Int, List<Op>>): Int {
    val mem = IntArray(6)
    val ipIndex = parsed.first
    while (mem[ipIndex] in parsed.second.indices) {
        val op = parsed.second[mem[ipIndex]]
        if (op.opCode != "seti" && (op.a == 0 || op.b == 0)) {
            mem[0] = mem[maxOf(op.a, op.b)]
        }
        if (debugPrint) print("ip=${mem[ipIndex]} $mem $op")
        opcodes.getValue(op.opCode)(mem, op)
        if (debugPrint) println("$mem")
        mem[ipIndex]++
    }
    return mem[0]
}

fun part2(parsed: Pair<Int, List<Op>>): Int {
    val mem = IntArray(6)
    val ipIndex = parsed.first
    var lastSeen = 0
    val seen = hashSetOf<Int>()
    val indexOfDiv = parsed.second.windowed(4).indexOfFirst {
        (it[0].opCode == "seti" && it[0].a == 0) &&
                (it[1].opCode == "addi" && it[1].b == 1 && it[1].a == it[0].dest) &&
                (it[2].opCode == "muli" && it[2].a == it[1].dest) &&
                (it[3].opCode == "gtrr" && it[3].a == it[2].dest)
    }
    while (mem[ipIndex] in parsed.second.indices) {
        val ip = mem[ipIndex]
        val op = parsed.second[ip]
        if (op.opCode != "seti" && (op.a == 0 || op.b == 0)) {
            val found = mem[maxOf(op.a, op.b)]
            if (!seen.add(found)) {
                return lastSeen
            } else {
                lastSeen = found
            }
        }
        if (debugPrint) print("ip=$ip ${mem.map { Integer.toHexString(it) }} $op")
        if (ip == indexOfDiv) {
            mem[op.dest] = (mem[parsed.second[indexOfDiv + 3].b] / parsed.second[indexOfDiv + 2].b) - 2
        } else {
            opcodes.getValue(op.opCode)(mem, op)
        }
        if (debugPrint) println("${mem.map { Integer.toHexString(it) }}")
        mem[ipIndex]++
    }
    return mem[0]
}