package mx.hmng.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import mx.hmng.app.data.local.dao.BitacoraDao
import mx.hmng.app.data.local.mapper.toDomain
import mx.hmng.app.data.local.mapper.toEntity
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.BitacoraMovimiento
import mx.hmng.app.domain.repository.BitacoraRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BitacoraRepositoryImpl @Inject constructor(
    private val apiService: HmngApiService,
    private val bitacoraDao: BitacoraDao
) : BitacoraRepository {

    override fun getBitacora(tipo: String?, pageSize: Int, offset: Int): Flow<List<BitacoraMovimiento>> =
        channelFlow {
            // Network-first: fetch from API, save to Room, Room emits via flow
            try {
                val response = apiService.getBitacora()
                if (response.isSuccessful) {
                    response.body()?.let { dtos ->
                        bitacoraDao.upsertAll(dtos.map { it.toEntity() })
                    }
                }
            } catch (_: Exception) {}

            // Emit from Room (including fallback data if network failed)
            launch {
                bitacoraDao.getPagedFlow(tipo, pageSize, offset).collect { entities ->
                    send(entities.map { it.toDomain() })
                }
            }
        }
}
