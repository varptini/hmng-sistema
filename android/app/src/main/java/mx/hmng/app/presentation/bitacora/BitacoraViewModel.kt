package mx.hmng.app.presentation.bitacora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import mx.hmng.app.domain.model.BitacoraMovimiento
import mx.hmng.app.domain.repository.BitacoraRepository
import javax.inject.Inject

data class BitacoraFilters(
    val tipo: String? = null,
    val insumoId: Int? = null,
    val desde: String? = null,
    val hasta: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BitacoraViewModel @Inject constructor(
    private val repo: BitacoraRepository
) : ViewModel() {

    private val _tipo = MutableStateFlow<String?>(null)
    private val _insumoId = MutableStateFlow<Int?>(null)
    private val _desde = MutableStateFlow<String?>(null)
    private val _hasta = MutableStateFlow<String?>(null)

    val tipo: StateFlow<String?> = _tipo.asStateFlow()
    val insumoId: StateFlow<Int?> = _insumoId.asStateFlow()
    val desde: StateFlow<String?> = _desde.asStateFlow()
    val hasta: StateFlow<String?> = _hasta.asStateFlow()

    val pagingFlow: Flow<PagingData<BitacoraMovimiento>> =
        combine(_tipo, _insumoId, _desde, _hasta) { t, i, d, h ->
            BitacoraFilters(t, i, d, h)
        }.flatMapLatest { f ->
            repo.getBitacora(
                tipo = f.tipo,
                insumoId = f.insumoId,
                desde = f.desde,
                hasta = f.hasta
            )
        }.cachedIn(viewModelScope)

    fun setFiltro(tipo: String? = _tipo.value, insumoId: Int? = _insumoId.value, desde: String? = _desde.value, hasta: String? = _hasta.value) {
        _tipo.value = tipo
        _insumoId.value = insumoId
        _desde.value = desde
        _hasta.value = hasta
    }

    fun setTipo(tipo: String?) {
        _tipo.value = tipo
    }

    fun setDesde(desde: String?) {
        _desde.value = desde
    }

    fun setHasta(hasta: String?) {
        _hasta.value = hasta
    }
}
