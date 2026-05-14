package mx.hmng.app.domain.usecase

import mx.hmng.app.data.session.SessionManager
import mx.hmng.app.domain.model.Usuario
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    operator fun invoke(): Usuario? = sessionManager.getUser()
}
