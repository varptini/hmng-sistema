package mx.hmng.app.domain.usecase.insumos

import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.repository.InsumoRepository
import javax.inject.Inject

class UpdateInsumoUseCase @Inject constructor(private val repo: InsumoRepository) {
    suspend operator fun invoke(id: Int, payload: Map<String, Any>): Result<Insumo> =
        repo.updateInsumo(id, payload)
}
