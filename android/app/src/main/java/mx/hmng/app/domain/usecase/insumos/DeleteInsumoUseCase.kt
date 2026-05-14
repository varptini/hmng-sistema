package mx.hmng.app.domain.usecase.insumos

import mx.hmng.app.domain.repository.InsumoRepository
import javax.inject.Inject

class DeleteInsumoUseCase @Inject constructor(private val repo: InsumoRepository) {
    suspend operator fun invoke(id: Int): Result<Unit> = repo.deleteInsumo(id)
}
