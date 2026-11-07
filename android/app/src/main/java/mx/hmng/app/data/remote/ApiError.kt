package mx.hmng.app.data.remote

import org.json.JSONObject
import retrofit2.Response

/** Excepción con el mensaje `error` devuelto por el backend en respuestas no 2xx. */
class ApiException(message: String, val code: Int) : Exception(message)

/**
 * Extrae el campo `error` del cuerpo JSON de una respuesta fallida
 * (formato del backend: `{ "error": "..." }`), con un mensaje genérico de respaldo.
 */
fun Response<*>.errorMessage(default: String = "Error del servidor (${code()})"): String {
    val raw = try {
        errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    if (raw.isNullOrBlank()) return default
    return try {
        JSONObject(raw).optString("error").takeIf { it.isNotBlank() } ?: default
    } catch (_: Exception) {
        default
    }
}

/** Lanza [ApiException] con el mensaje del backend si la respuesta no fue exitosa. */
fun <T> Response<T>.requireSuccess(): Response<T> {
    if (!isSuccessful) throw ApiException(errorMessage(), code())
    return this
}

/** Devuelve el cuerpo de una respuesta exitosa o lanza [ApiException] con el error del backend. */
fun <T> Response<T>.bodyOrThrow(): T {
    requireSuccess()
    return body() ?: throw ApiException("Respuesta vacía del servidor", code())
}
