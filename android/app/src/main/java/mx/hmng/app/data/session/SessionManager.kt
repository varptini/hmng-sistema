package mx.hmng.app.data.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import mx.hmng.app.domain.model.Usuario
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "hmng_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUser(usuario: Usuario) {
        prefs.edit()
            .putInt("id", usuario.id)
            .putString("nombre", usuario.nombre)
            .putString("email", usuario.email)
            .putString("rol", usuario.rol)
            .putString("token", usuario.token)
            .apply()
    }

    fun getUser(): Usuario? {
        val id = prefs.getInt("id", -1).takeIf { it != -1 } ?: return null
        return Usuario(
            id = id,
            nombre = prefs.getString("nombre", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            rol = prefs.getString("rol", "") ?: "",
            token = prefs.getString("token", "") ?: ""
        )
    }

    fun clearSession() = prefs.edit().clear().apply()

    val isLoggedIn: Boolean get() = getUser() != null
}
