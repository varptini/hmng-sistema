package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.BitacoraMovimiento

interface BitacoraRepository {
    fun getBitacora(tipo: String? = null, pageSize: Int = 50, offset: Int = 0): Flow<List<BitacoraMovimiento>>
}
