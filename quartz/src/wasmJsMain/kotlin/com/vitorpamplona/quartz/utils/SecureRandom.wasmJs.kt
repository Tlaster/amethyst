/*
 * Copyright (c) 2025 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.quartz.utils

/** Fills a single Int32Array(1) with Web Crypto randomness and returns the int value. */
@JsFun("() => { const a = new Int32Array(1); globalThis.crypto.getRandomValues(a); return a[0]; }")
private external fun randomInt32(): Int

actual class SecureRandom {
    actual fun nextInt(): Int = randomInt32()

    actual fun nextInt(bound: Int): Int {
        require(bound > 0) { "Bad Bound $bound" }
        var intValue = nextPositiveInt()
        val m = bound - 1
        if ((bound and m) == 0) {
            intValue = ((bound * intValue.toLong()) shr 31).toInt()
        } else {
            var u = intValue
            intValue = u % bound
            while (u - intValue + m < 0) {
                u = nextPositiveInt()
                intValue = u % bound
            }
        }
        return intValue
    }

    actual fun nextLong(): Long {
        val hi = randomInt32().toLong() and 0xFFFFFFFFL
        val lo = randomInt32().toLong() and 0xFFFFFFFFL
        return (hi shl 32) or lo
    }

    actual fun nextLong(bound: Long): Long {
        require(bound > 0) { "Bad Bound $bound" }
        if (bound < Int.MAX_VALUE) return nextInt(bound.toInt()).toLong()
        return nextPositiveLong() % bound
    }

    fun nextPositiveInt(): Int {
        val v = nextInt()
        return if (v > 0) v else -v
    }

    fun nextPositiveLong(): Long {
        val v = nextLong()
        return if (v > 0) v else -v
    }

    actual fun nextBytes(output: ByteArray) {
        var i = 0
        while (i < output.size) {
            val v = randomInt32()
            output[i] = v.toByte()
            if (i + 1 < output.size) output[i + 1] = (v ushr 8).toByte()
            if (i + 2 < output.size) output[i + 2] = (v ushr 16).toByte()
            if (i + 3 < output.size) output[i + 3] = (v ushr 24).toByte()
            i += 4
        }
    }
}
