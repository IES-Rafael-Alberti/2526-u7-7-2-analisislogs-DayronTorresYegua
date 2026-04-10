package org.iesra

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ParseadorLog {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun parsear(linea: String): EntradaLog? {
        return try {
            val fechaTexto = linea.substringAfter("[").substringBefore("]")
            val resto = linea.substringAfter("] ").split(" ", limit = 2)

            if (resto.size < 2) return null

            val nivel = try {
                LogLevel.valueOf(resto[0])
            } catch (e: Exception) {
                return null
            }

            val mensaje = resto[1]
            val fecha = LocalDateTime.parse(fechaTexto, formatter)

            EntradaLog(fecha, nivel, mensaje)

        } catch (e: Exception) {
            null
        }
    }
}