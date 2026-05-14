package mx.hmng.app.domain.usecase.insumos

import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.repository.InsumoRepository
import javax.inject.Inject

class CreateInsumoUseCase @Inject constructor(private val repo: InsumoRepository) {
    suspend operator fun invoke(payload: Map<String, Any>): Result<Insumo> =
        repo.createInsumo(payload)
}
