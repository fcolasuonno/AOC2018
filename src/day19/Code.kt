package day19

import java.io.File

private const val debugPrint = false

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

typealias Mem = MutableList<Int>

data class Op(val opCode: String, val a: Int, val b: Int, val dest: Int)

fun part1(parsed: Pair<Int, List<Op>>): Int {
    val mem = MutableList(6) { 0 }
    val ipIndex = parsed.first
    while (mem[ipIndex] in parsed.second.indices) {
        val op = parsed.second[mem[ipIndex]]
        if (debugPrint) print("ip=${mem[parsed.first]} $mem $op")
        opcodes.getValue(op.opCode)(mem, op)
        if (debugPrint) println("$mem")
        mem[ipIndex]++
    }
    return mem[0]
}

fun part2(parsed: Pair<Int, List<Op>>): Int {
    val mem = MutableList(6) { if (it == 0) 1 else 0 }
    val ipIndex = parsed.first
    repeat(100) {
        val op = parsed.second[mem[ipIndex]]
        opcodes.getValue(op.opCode)(mem, op)
        mem[ipIndex]++
    }
    val factorize = mem.max()!!
    return (1..factorize).filter { factorize % it == 0 }.sum()
}