package com.flyfishxu.kadb.cert

import org.bouncycastle.asn1.x509.Time
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

object KadbCert {
    private var isSet = false
    internal var cert = byteArrayOf()
    internal var key = byteArrayOf()

    fun set(cert: ByteArray, key: ByteArray) {
        if (isSet) {
            throw IllegalStateException("Certificate or Private key is already set")
        }
        this.cert = cert
        this.key = key
        isSet = true
    }

    fun getOrError(): Pair<ByteArray, ByteArray> {
        if (cert.isEmpty() || key.isEmpty()) {
            throw IllegalStateException("Certificate or Private key is not set")
        }
        return cert to key
    }

    fun get(
        keySize: Int = 2048,
        cn: String = "Kadb",
        ou: String = "Kadb",
        o: String = "Kadb",
        l: String = "Kadb",
        st: String = "Kadb",
        c: String = "Kadb",
        notAfter: Time = Time(Date(System.currentTimeMillis() + 10368000000)), // 120 days
        serialNumber: BigInteger = BigInteger(64, SecureRandom())
    ): Pair<ByteArray, ByteArray> {
        if (cert.isEmpty() || key.isEmpty()) {
            generate(
                keySize, cn, ou, o, l, st, c, notAfter, serialNumber
            )
        }
        return cert to key
    }
}