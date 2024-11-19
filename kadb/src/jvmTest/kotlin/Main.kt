import com.flyfishxu.kadb.Kadb
import com.flyfishxu.kadb.cert.KadbCert

@OptIn(ExperimentalStdlibApi::class)
private suspend fun main() {
    val ip = "10.0.0.229"
    val pairCode = "790316"
    val pairPort = 40735
    val connPort = 37929

    println("Pairing")
    Kadb.pair(ip, pairPort, pairCode)
    println("Paired")
    println(KadbCert.cert.toHexString())

    println("Get device info")
    val result1 = Kadb.create(ip, connPort).shell("getprop ro.product.model")
    println(result1)
}