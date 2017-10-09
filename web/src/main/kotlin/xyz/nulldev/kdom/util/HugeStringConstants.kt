package xyz.nulldev.kdom.util

internal val HUGE_STRING = "abcdefghijklmnopqrstuvwxyz".let {
    val rand = Random(it.length.toLong())
    (1 .. 10).joinToString(separator = "") { _ ->
         it[(rand.nextLong() % it.length).toInt()].toString()
    }
}

internal class Random(seed: Long) {
    var seed = (seed % 2147483647).let {
        if (it <= 0)
            it + 2147483646
        else it
    }

    fun nextLong(): Long {
        seed = seed * 16807 % 2147483647
        return seed
    }

    fun nextFloat() = (nextLong() - 1) / 2147483646f
}

