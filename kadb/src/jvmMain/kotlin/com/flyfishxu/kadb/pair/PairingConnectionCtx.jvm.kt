package com.flyfishxu.kadb.pair

internal actual fun PairingConnectionCtx.getConscryptClass(): Class<*> {
    return Class.forName("org.conscrypt.Conscrypt")
}