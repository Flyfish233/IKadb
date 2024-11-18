package com.flyfishxu.kadb

import java.io.IOException
import kotlin.Throws

/**
 * Value of the "packet kind" byte in a [shell v2 packet]
 */
internal const val ID_STDIN = 0
internal const val ID_STDOUT = 1
internal const val ID_STDERR = 2
internal const val ID_EXIT = 3
internal const val ID_CLOSE_STDIN = 4
internal const val ID_WINDOW_SIZE_CHANGE = 5
internal const val ID_INVALID = 255

class AdbShellStream(
    private val stream: AdbStream
) : AutoCloseable {
    @Throws(IOException::class)
    fun readAll(): AdbShellResponse {
        val output = StringBuilder()
        val errorOutput = StringBuilder()
        while (true) {
            when (val packet = read()) {
                is AdbShellPacket.Exit -> {
                    val exitCode = packet.payload[0].toInt()
                    return AdbShellResponse(output.toString(), errorOutput.toString(), exitCode)
                }

                is AdbShellPacket.StdOut -> {
                    output.append(String(packet.payload))
                }

                is AdbShellPacket.StdError -> {
                    errorOutput.append(String(packet.payload))
                }

                else -> {
                    throw IllegalStateException("Unexpected shell packet: $packet")
                }
            }
        }
    }

    @Throws(IOException::class)
    fun read(): AdbShellPacket {
        stream.source.apply {
            val id = checkId(readByte().toInt())
            val length = checkLength(id, readIntLe())
            val payload = readByteArray(length.toLong())
            return when (id) {
                ID_STDOUT -> AdbShellPacket.StdOut(payload)
                ID_STDERR -> AdbShellPacket.StdError(payload)
                ID_EXIT -> AdbShellPacket.Exit(payload)
                ID_CLOSE_STDIN -> throw IOException("Todo: ID_CLOSE_STDIN")
                ID_WINDOW_SIZE_CHANGE -> throw IOException("Todo: ID_WINDOW_SIZE_CHANGE")
                ID_INVALID -> throw IOException("Todo: ID_INVALID")
                else -> throw IllegalArgumentException("Invalid shell packet id: $id")
            }
        }
    }

    @Throws(IOException::class)
    fun write(string: String) {
        write(ID_STDIN, string.toByteArray())
    }

    @Throws(IOException::class)
    fun write(id: Int, payload: ByteArray? = null) {
        stream.sink.apply {
            writeByte(id)
            writeIntLe(payload?.size ?: 0)
            if (payload != null) write(payload)
            flush()
        }
    }

    override fun close() {
        stream.close()
    }

    private fun checkId(id: Int): Int {
        check(id == ID_STDOUT || id == ID_STDERR || id == ID_EXIT) {
            "Invalid shell packet id: $id"
        }
        return id
    }

    private fun checkLength(id: Int, length: Int): Int {
        check(length >= 0) { "Shell packet length must be >= 0: $length" }
        check(id != ID_EXIT || length == 1) { "Shell exit packet does not have payload length == 1: $length" }
        return length
    }
}