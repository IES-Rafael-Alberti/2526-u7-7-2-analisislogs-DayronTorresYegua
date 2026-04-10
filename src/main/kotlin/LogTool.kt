package org.iesra

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LogTool : CliktCommand() {

    private val input by option("-i", "--input").required()
    private val from by option("-f", "--from")
    private val to by option("-t", "--to")
    private val level by option("-l", "--level")
    private val stats by option("-s", "--stats").flag(default = false)
    private val output by option("-o", "--output")
    private val stdout by option("-p", "--stdout").flag(default = false)
    private val report by option("-r", "--report").flag(default = false)
    private val ignoreInvalid by option("--ignore-invalid").flag(default = false)
    private val formatterSalida = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private fun formatearFecha(fecha: LocalDateTime): String {
        return fecha.format(formatterSalida)
    }
    override fun run() {

        if (stats && report) {
            echo("Error: no puedes usar --stats y --report a la vez")
            return
        }

        if (!stdout && output == null) {
            echo("Error: debes indicar --stdout o --output")
            return
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val parser = ParseadorLog()

        val lineas = File(input).readLines()

        var validas = 0
        var invalidas = 0

        var info = 0
        var warning = 0
        var error = 0

        val logs = mutableListOf<EntradaLog>()

        for (linea in lineas) {
            val log = parser.parsear(linea)

            if (log != null) {
                validas++
                logs.add(log)

                when (log.nivel) {
                    LogLevel.INFO -> info++
                    LogLevel.WARNING -> warning++
                    LogLevel.ERROR -> error++
                }
            } else {
                invalidas++
                if (!ignoreInvalid) {
                    echo("Línea inválida: $linea")
                }
            }
        }

        val fechaInicio = from?.let { LocalDateTime.parse(it, formatter) }
        val fechaFinal = to?.let { LocalDateTime.parse(it, formatter) }

        val logsFiltrados = logs.filter { log ->
            val despuesInicio = fechaInicio?.let { log.fecha >= it } ?: true
            val antesFinal = fechaFinal?.let { log.fecha <= it } ?: true
            despuesInicio && antesFinal
        }

        val niveles = level?.split(",")?.mapNotNull {
            try {
                LogLevel.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }

        val logsFinales = if (niveles != null) {
            logsFiltrados.filter { it.nivel in niveles }
        } else {
            logsFiltrados
        }

        val primeraFecha = logsFinales.minByOrNull { it.fecha }?.let { formatearFecha(it.fecha) }
        val ultimaFecha = logsFinales.maxByOrNull { it.fecha }?.let { formatearFecha(it.fecha) }

        val resultado = StringBuilder()

        resultado.appendLine("INFORME DE LOGS")
        resultado.appendLine("===============")
        resultado.appendLine("Fichero: $input")
        resultado.appendLine("Rango: ${from ?: "-"} -> ${to ?: "-"}")
        resultado.appendLine("Niveles: ${level ?: "TODOS"}")
        resultado.appendLine()

        resultado.appendLine("Resumen:")
        resultado.appendLine("- Procesadas: ${lineas.size}")
        resultado.appendLine("- Válidas: $validas")
        resultado.appendLine("- Inválidas: $invalidas")
        resultado.appendLine()

        resultado.appendLine("Conteo:")
        resultado.appendLine("- INFO: $info")
        resultado.appendLine("- WARNING: $warning")
        resultado.appendLine("- ERROR: $error")
        resultado.appendLine()

        resultado.appendLine("Periodo detectado:")
        resultado.appendLine("- Primera: $primeraFecha")
        resultado.appendLine("- Última: $ultimaFecha")

        if (!stats) {
            resultado.appendLine()
            resultado.appendLine("LINEAS FILTRADAS:")
            logsFinales.forEach {
                resultado.appendLine("[${formatearFecha(it.fecha)}] ${it.nivel} ${it.mensaje}")
            }
        }

        when {
            stdout -> println(resultado.toString())
            output != null -> File(output).writeText(resultado.toString())
            else -> println(resultado.toString())
        }
    }
}