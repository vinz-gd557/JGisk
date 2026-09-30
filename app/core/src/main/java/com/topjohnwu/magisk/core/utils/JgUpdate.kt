package com.topjohnwu.magisk.core.utils

import com.topjohnwu.magisk.core.BuildConfig

/** Perbandingan versi JGisk: tag release "v1.2" vs versi app "JGisk-1.2". */
object JgUpdate {
    private fun nums(s: String): List<Int> =
        Regex("\\d+").findAll(s).mapNotNull { it.value.toIntOrNull() }.toList()

    fun code(tag: String): Int {
        val n = nums(tag)
        return n.getOrElse(0) { 0 } * 10000 + n.getOrElse(1) { 0 } * 100 + n.getOrElse(2) { 0 }
    }

    fun isNewer(remote: String): Boolean {
        val a = nums(remote)
        val b = nums(BuildConfig.APP_VERSION_NAME)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
