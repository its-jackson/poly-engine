package org.poly.engine

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Logger(private val header: String) {
    // Green
    fun info(msg: Any?) = log("Info", "\u001B[32m", msg)

    // Red
    fun error(msg: Any?) = log("Error", "\u001B[31m", msg)

    // Yellow
    fun warn(msg: Any?) = log("Warn", "\u001B[33m", msg)

    // Blue
    fun debug(msg: Any?) = log("Debug", "\u001B[34m", msg)

    // Magenta
    fun critical(msg: Any?) = log("Critical", "\u001B[35m", msg)

    private fun log(
        level: String,
        color: String,
        msg: Any?
    ) = LocalDateTime.now().format(dateTimeFormatter).let {
        println("$color[$it] [$level] [$header] $msg\u001B[0m")
    }

    private companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
