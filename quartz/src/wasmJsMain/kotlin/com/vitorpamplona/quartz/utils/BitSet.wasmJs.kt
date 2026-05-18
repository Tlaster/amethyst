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

/** Pure-Kotlin ByteArray-backed BitSet for wasmJs (single-threaded). */
actual class BitSet {
    private val bytes: ByteArray
    private val nBits: Int

    actual constructor(nBits: Int) {
        this.nBits = nBits
        this.bytes = ByteArray((nBits + 7) / 8)
    }

    private constructor(src: ByteArray) {
        this.bytes = src.copyOf()
        this.nBits = src.size * 8
    }

    actual fun set(bitIndex: Int) {
        bytes[bitIndex ushr 3] = (bytes[bitIndex ushr 3].toInt() or (1 shl (bitIndex and 7))).toByte()
    }

    actual fun clear(bitIndex: Int) {
        bytes[bitIndex ushr 3] = (bytes[bitIndex ushr 3].toInt() and (1 shl (bitIndex and 7)).inv()).toByte()
    }

    actual fun get(bitIndex: Int): Boolean = (bytes[bitIndex ushr 3].toInt() ushr (bitIndex and 7)) and 1 == 1

    actual fun size(): Int = nBits

    actual fun toByteArray(): ByteArray = bytes.copyOf()

    companion object {
        fun fromBytes(src: ByteArray): BitSet = BitSet(src)
    }
}

actual fun bitSetValueOf(bytes: ByteArray): BitSet = BitSet.fromBytes(bytes)
