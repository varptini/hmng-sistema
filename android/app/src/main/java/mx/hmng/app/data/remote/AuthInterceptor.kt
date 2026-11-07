package mx.hmng.app.data.remote

import mx.hmng.app.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adjunta `Authorization: Bearer <token>` a cada petición cuando hay sesión activa.
 * Si el backend responde 401 a una petición autenticada (token expirado/inválido o
 * usuario desactivado), limpia la sesión; HmngNavHost redirige a Login en el
 * siguiente cambio de navegación.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.getUser()?.token?.takeIf { it.isNotBlank() }

        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        val response = chain.proceed(request)

        if (response.code == 401 && token != null) {
            sessionManager.clearSession()
        }

        return response
    }
}
