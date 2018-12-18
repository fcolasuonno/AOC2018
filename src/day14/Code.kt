package day14

fun main(args: Array<String>) {
    val input = if (false) 59414 else 110201
    println("Part 1 = ${part1(input)}")
    println("Part 2 = ${part2(input)}")
}

data class Recipe(val value: Short, var next: Recipe? = null) {
    fun add(i: Short) = Recipe(i).also { this.next = it }
}

data class CircularBuffer(var start: Short, val size: Int = 10) {
    val root = Recipe(start)
    var tail = root
    var next = root
    var count = 1
    fun add(i: Short) {
        next = next.add(i)
        count++
        if (count > size) {
            tail = tail.next!!
        }
    }

    fun matches(required: List<Short>): Boolean {
        var cursor: Recipe? = tail
        var i = 0
        while (cursor?.value == required[i++]) {
            cursor = cursor.next
        }
        return cursor == null && (i == required.size)
    }
}

fun part1(input: Int): String {
    val buffer = CircularBuffer(3)
    buffer.add(7)
    var elf1 = buffer.root
    var elf2 = buffer.root.next!!
    while (buffer.count < input + 10) {
        val recipe1 = elf1.value
        val recipe2 = elf2.value
        var sum = recipe1 + recipe2
        if (sum >= 10) {
            buffer.add((sum / 10).toShort())
            sum %= 10
        }
        buffer.add(sum.toShort())
        repeat(recipe1 + 1) {
            elf1 = elf1.next ?: buffer.root
        }
        repeat(recipe2 + 1) {
            elf2 = elf2.next ?: buffer.root
        }
    }
    var lastRecipes: Recipe? = buffer.tail
    return buildString {
        do {
            append(lastRecipes?.value)
            lastRecipes = lastRecipes?.next
        } while (lastRecipes != null)
    }
}

fun part2(input: Int): Int {
    val required = input.toString().map { it - '0' }.map { it.toShort() }
    val buffer = CircularBuffer(3, required.size)
    buffer.add(7)
    var elf1 = buffer.root
    var elf2 = buffer.root.next!!
    while (!buffer.matches(required)) {
        val recipe1 = elf1.value
        val recipe2 = elf2.value
        var sum = recipe1 + recipe2
        if (sum >= 10) {
            buffer.add((sum / 10).toShort())
            sum %= 10
            if (buffer.matches(required)) {
                break
            }
        }
        buffer.add(sum.toShort())
        repeat(recipe1 + 1) {
            elf1 = elf1.next ?: buffer.root
        }
        repeat(recipe2 + 1) {
            elf2 = elf2.next ?: buffer.root
        }
    }
    return buffer.count - required.size
}
