package mx.hmng.app.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val fechaHoraFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

/** Convierte un timestamp ISO-8601 del backend (UTC) a "dd/MM/yyyy HH:mm" en la zona local. */
fun formatFechaHora(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).format(fechaHoraFormatter)
    } catch (_: Exception) {
        iso.take(16).replace('T', ' ')
    }
}
