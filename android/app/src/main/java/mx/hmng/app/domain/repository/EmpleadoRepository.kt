package mx.hmng.app.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.hmng.app.domain.model.Empleado

interface EmpleadoRepository {
    fun getEmpleados(): Flow<List<Empleado>>
    suspend fun refresh(): Result<Unit>
    suspend fun createEmpleado(payload: Map<String, Any>): Result<Unit>
    suspend fun updateEmpleado(id: Int, payload: Map<String, Any>): Result<Unit>
    suspend fun deleteEmpleado(id: Int): Result<Unit>
}
