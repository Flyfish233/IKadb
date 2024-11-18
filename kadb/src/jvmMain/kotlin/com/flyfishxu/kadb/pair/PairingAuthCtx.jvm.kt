package com.flyfishxu.kadb.pair

import io.github.muntashirakon.crypto.spake2.Spake2Context
import io.github.muntashirakon.crypto.spake2.Spake2Role
import org.bouncycastle.crypto.InvalidCipherTextException
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.modes.GCMBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import javax.security.auth.Destroyable

internal actual class PairingAuthCtx(
    private val mSpake2Ctx: Spake2Context, password: ByteArray
) : Destroyable {
    actual val msg: ByteArray = mSpake2Ctx.generateMessage(password)
    private val mSecretKey = ByteArray(HKDF_KEY_LENGTH)
    private var mDecIv: Long = 0
    private var mEncIv: Long = 0
    private var mIsDestroyed = false

    actual fun initCipher(theirMsg: ByteArray?): Boolean {
        if (mIsDestroyed) return false
        val keyMaterial = mSpake2Ctx.processMessage(theirMsg) ?: return false
        val hkdf = HKDFBytesGenerator(SHA256Digest())
        hkdf.init(HKDFParameters(keyMaterial, null, INFO))
        hkdf.generateBytes(mSecretKey, 0, mSecretKey.size)
        return true
    }

    actual fun encrypt(input: ByteArray): ByteArray? {
        return encryptDecrypt(
            true, input, ByteBuffer.allocate(GCM_IV_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putLong(mEncIv++).array()
        )
    }

    actual fun decrypt(input: ByteArray): ByteArray? {
        return encryptDecrypt(
            false, input, ByteBuffer.allocate(GCM_IV_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putLong(mDecIv++).array()
        )
    }

    actual override fun isDestroyed(): Boolean {
        return mIsDestroyed
    }

    actual override fun destroy() {
        mIsDestroyed = true
        Arrays.fill(mSecretKey, 0.toByte())
        mSpake2Ctx.destroy()
    }

    private fun encryptDecrypt(forEncryption: Boolean, `in`: ByteArray, iv: ByteArray): ByteArray? {
        if (mIsDestroyed) return null
        val spec = AEADParameters(KeyParameter(mSecretKey), mSecretKey.size * 8, iv)
        val cipher = GCMBlockCipher.newInstance(AESEngine.newInstance())
        cipher.init(forEncryption, spec)
        val out = ByteArray(cipher.getOutputSize(`in`.size))
        val newOffset = cipher.processBytes(`in`, 0, `in`.size, out, 0)
        try {
            cipher.doFinal(out, newOffset)
        } catch (e: InvalidCipherTextException) {
            return null
        }
        return out
    }

    actual companion object
}

internal actual fun PairingAuthCtx.Companion.createAlice(password: ByteArray): PairingAuthCtx? {
    val spake25519 = Spake2Context(Spake2Role.Alice, CLIENT_NAME, SERVER_NAME)
    return try {
        PairingAuthCtx(spake25519, password)
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: IllegalStateException) {
        null
    }
}