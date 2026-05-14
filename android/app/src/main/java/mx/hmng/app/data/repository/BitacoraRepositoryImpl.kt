package mx.hmng.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import mx.hmng.app.data.local.mapper.toDomain
import mx.hmng.app.data.remote.api.HmngApiService
import mx.hmng.app.domain.model.BitacoraMovimiento
import mx.hmng.app.domain.repository.BitacoraRepository
import java.io.IOException
import javax.inject.Inject

class BitacoraRepositoryImpl @Inject constructor(
    private val api: HmngApiService
) : BitacoraRepository {

    override fun getBitacora(
        tipo: String?,
        insumoId: Int?,
        desde: String?,
        hasta: String?
    ): Flow<PagingData<BitacoraMovimiento>> = Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            BitacoraApiPagingSource(api, tipo, insumoId, desde, hasta)
        }
    ).flow
}

private class BitacoraApiPagingSource(
    private val api: HmngApiService,
    private val tipo: String?,
    private val insumoId: Int?,
    private val desde: String?,
    private val hasta: String?
) : PagingSource<Int, BitacoraMovimiento>() {

    override fun getRefreshKey(state: PagingState<Int, BitacoraMovimiento>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BitacoraMovimiento> {
        val page = params.key ?: 1
        return try {
            val response = api.getBitacora(
                page = page,
                tipo = tipo,
                insumoId = insumoId,
                desde = desde,
                hasta = hasta
            )
            if (!response.isSuccessful) {
                return LoadResult.Error(IOException("HTTP ${response.code()}"))
            }
            val items = response.body() ?: emptyList()
            LoadResult.Page(
                data = items.map { it.toDomain() },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (items.size < 50) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
