package mx.hmng.app.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.BitacoraMovimiento

interface BitacoraRepository {
    fun getBitacora(
        tipo: String? = null,
        insumoId: Int? = null,
        desde: String? = null,
        hasta: String? = null
    ): Flow<PagingData<BitacoraMovimiento>>
}
