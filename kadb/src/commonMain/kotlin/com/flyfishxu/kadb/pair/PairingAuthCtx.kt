/*
 * Copyright (c) 2024 Flyfish-Xu
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.flyfishxu.kadb.pair

import javax.security.auth.Destroyable

internal expect class PairingAuthCtx : Destroyable {
    val msg: ByteArray
    fun initCipher(theirMsg: ByteArray?): Boolean
    fun encrypt(input: ByteArray): ByteArray?
    fun decrypt(input: ByteArray): ByteArray?
    override fun isDestroyed(): Boolean
    override fun destroy()

    companion object
}

internal val PairingAuthCtx.Companion.GCM_IV_LENGTH: Int  // in bytes
    get() = 12

// The following values are taken from the following source and are subjected to change
// https://github.com/aosp-mirror/platform_system_core/blob/android-11.0.0_r1/adb/pairing_auth/pairing_auth.cpp
internal val PairingAuthCtx.Companion.CLIENT_NAME: ByteArray
    get() = StringCompat.getBytes("adb pair client\u0000", "UTF-8")
internal val PairingAuthCtx.Companion.SERVER_NAME: ByteArray
    get() = StringCompat.getBytes("adb pair server\u0000", "UTF-8")

// The following values are taken from the following source and are subjected to change
// https://github.com/aosp-mirror/platform_system_core/blob/android-11.0.0_r1/adb/pairing_auth/aes_128_gcm.cpp
internal val PairingAuthCtx.Companion.INFO: ByteArray
    get() = StringCompat.getBytes("adb pairing_auth aes-128-gcm key", "UTF-8")

internal val PairingAuthCtx.Companion.HKDF_KEY_LENGTH: Int
    get() = 128 / 8

internal expect fun PairingAuthCtx.Companion.createAlice(password: ByteArray): PairingAuthCtx?