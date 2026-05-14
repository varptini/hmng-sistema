package mx.hmng.app.domain.usecase.insumos

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.Insumo
import mx.hmng.app.domain.repository.InsumoRepository
import javax.inject.Inject

class GetAlertasUseCase @Inject constructor(private val repo: InsumoRepository) {
    fun caducados(): Flow<List<Insumo>> = repo.getCaducados()
    fun porCaducar(): Flow<List<Insumo>> = repo.getPorCaducar()
    fun stockBajo(): Flow<List<Insumo>> = repo.getStockBajo()
}
