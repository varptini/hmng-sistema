package mx.hmng.app.domain.usecase.auth

import mx.hmng.app.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(private val repo: AuthRepository) {
    operator fun invoke() = repo.getCurrentUser()
}
