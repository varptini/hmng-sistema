package mx.hmng.app.domain.usecase.insumos

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.repository.InsumoRepository
import javax.inject.Inject

class GetInsumosUseCase @Inject constructor(private val repo: InsumoRepository) {
    operator fun invoke(query: String = "", alerta: String? = null): Flow<List<Insumo>> =
        repo.getInsumos(query, alerta)
}
