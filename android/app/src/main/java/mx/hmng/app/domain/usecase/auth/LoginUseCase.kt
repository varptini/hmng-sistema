package mx.hmng.app.domain.usecase.auth

import mx.hmng.app.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(usuario: String, contrasena: String) =
        repo.login(usuario, contrasena)
}
