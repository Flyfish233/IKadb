package com.flyfishxu.kadb.debug

import com.flyfishxu.kadb.Kadb
import com.flyfishxu.kadb.cert.KadbCert

@OptIn(ExperimentalStdlibApi::class)
private suspend fun main() {
    val ip = "10.0.0.229"
    val pairCode = "374027"
    val pairPort = 43373
    val connPort = 38857

    println("Pairing")
    Kadb.pair(ip, pairPort, pairCode)
    println("Paired")
    println(KadbCert.cert.toHexString())

    val result = Kadb.create(ip, connPort).shell("ls -l")
    println(result)
}