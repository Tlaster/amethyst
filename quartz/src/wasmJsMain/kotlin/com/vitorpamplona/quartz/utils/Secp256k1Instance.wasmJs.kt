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

import com.vitorpamplona.quartz.utils.secp256k1.ECPoint
import com.vitorpamplona.quartz.utils.secp256k1.KeyCodec
import com.vitorpamplona.quartz.utils.secp256k1.MutablePoint
import com.vitorpamplona.quartz.utils.secp256k1.ScalarN
import com.vitorpamplona.quartz.utils.secp256k1.U256

actual object Secp256k1Instance {
    private val h02 = Hex.decode("02")

    actual fun compressedPubKeyFor(privKey: ByteArray): ByteArray = Secp256k1InstanceKotlin.compressedPubKeyFor(privKey)

    actual fun isPrivateKeyValid(il: ByteArray): Boolean = Secp256k1InstanceKotlin.isPrivateKeyValid(il)

    actual fun signSchnorr(
        data: ByteArray,
        privKey: ByteArray,
        nonce: ByteArray?,
    ): ByteArray = Secp256k1InstanceKotlin.signSchnorr(data, privKey, nonce)

    actual fun signSchnorr(
        data: ByteArray,
        privKey: ByteArray,
    ): ByteArray = Secp256k1InstanceKotlin.signSchnorr(data, privKey)

    actual fun signSchnorrWithXOnlyPubKey(
        data: ByteArray,
        privKey: ByteArray,
        xOnlyPubKey: ByteArray,
        nonce: ByteArray?,
    ): ByteArray = Secp256k1InstanceKotlin.signSchnorrWithXOnlyPubKey(data, privKey, xOnlyPubKey, nonce)

    actual fun verifySchnorr(
        signature: ByteArray,
        hash: ByteArray,
        pubKey: ByteArray,
    ): Boolean = Secp256k1InstanceKotlin.verifySchnorr(signature, hash, pubKey)

    actual fun privateKeyAdd(
        first: ByteArray,
        second: ByteArray,
    ): ByteArray = Secp256k1InstanceKotlin.privateKeyAdd(first, second)

    actual fun pubKeyTweakMulCompact(
        pubKey: ByteArray,
        privateKey: ByteArray,
    ): ByteArray = Secp256k1InstanceKotlin.pubKeyTweakMulCompact(pubKey, privateKey)

    actual fun pubKeyTweakAdd(
        pubKey: ByteArray,
        tweak: ByteArray,
    ): ByteArray {
        val sc = ECPoint.getScratch()
        val full = if (pubKey.size == 32) h02 + pubKey else pubKey
        check(KeyCodec.parsePublicKey(full, sc.entryPx, sc.entryPy))
        sc.entryPoint.setAffine(sc.entryPx, sc.entryPy)

        val tweakScalar = sc.scalarTmp1
        U256.fromBytesInto(tweakScalar, tweak, 0)
        require(ScalarN.isValid(tweakScalar))

        // Compute tweak * G into a fresh MutablePoint (mulG uses sc.dblCopy/mixTmp internally)
        val tweakPoint = MutablePoint()
        ECPoint.mulG(tweakPoint, tweakScalar, sc)

        ECPoint.addPoints(sc.entryResult, sc.entryPoint, tweakPoint, sc)
        check(ECPoint.toAffine(sc.entryResult, sc.entryPx, sc.entryPy, sc))
        return KeyCodec.serializeCompressed(sc.entryPx, sc.entryPy)
    }

    actual fun privKeyNegate(privKey: ByteArray): ByteArray {
        val a = U256.fromBytes(privKey)
        val result = ScalarN.neg(a)
        return U256.toBytes(result)
    }
}
