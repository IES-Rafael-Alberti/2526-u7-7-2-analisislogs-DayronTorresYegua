package org.iesra

import java.time.LocalDateTime

data class EntradaLog(
    val fecha: LocalDateTime,
    val nivel: LogLevel,
    val mensaje: String
)