package com.flyfishxu.kadb.shell

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