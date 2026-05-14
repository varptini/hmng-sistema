package mx.hmng.app.domain.usecase.auth

import mx.hmng.app.domain.repository.AuthRepository
import javax.inject.Inject

class CambiarContrasenaUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(actual: String, nueva: String) =
        repo.cambiarContrasena(actual, nueva)
}
