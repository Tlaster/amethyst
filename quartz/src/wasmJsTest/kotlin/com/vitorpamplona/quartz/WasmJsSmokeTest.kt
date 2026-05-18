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
package com.vitorpamplona.quartz

import com.vitorpamplona.quartz.utils.SecureRandom
import com.vitorpamplona.quartz.utils.currentTimeSeconds
import com.vitorpamplona.quartz.utils.platform
import com.vitorpamplona.quartz.utils.sha256.sha256
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WasmJsSmokeTest {
    @Test
    fun platformIsWasmJs() {
        assertEquals("WasmJs", platform())
    }

    @Test
    fun currentTimeSecondsIsReasonable() {
        val t = currentTimeSeconds()
        assertTrue(t > 1_700_000_000L, "Expected Unix timestamp > 1_700_000_000 but got $t")
    }

    @Test
    fun secureRandomProducesBytes() {
        val rng = SecureRandom()
        val bytes = ByteArray(32)
        rng.nextBytes(bytes)
        // Very unlikely to be all zeros
        assertTrue(bytes.any { it != 0.toByte() }, "SecureRandom produced all-zero bytes")
    }

    @Test
    fun sha256HelloWorld() {
        val hash = sha256("hello world".encodeToByteArray())
        assertEquals(32, hash.size)
        // SHA-256("hello world") = b94d27b9...
        assertEquals(0xb9.toByte(), hash[0])
        assertEquals(0x4d.toByte(), hash[1])
    }
}
