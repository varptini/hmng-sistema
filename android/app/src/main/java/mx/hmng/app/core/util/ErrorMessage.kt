package mx.hmng.app.core.util

import java.io.IOException

/** Mensaje legible para el usuario a partir de un error de red o del backend. */
fun Throwable.userMessage(default: String = "Ocurrió un error inesperado"): String = when (this) {
    is IOException -> "No se pudo conectar con el servidor. Revisa tu conexión."
    else -> message?.takeIf { it.isNotBlank() } ?: default
}
