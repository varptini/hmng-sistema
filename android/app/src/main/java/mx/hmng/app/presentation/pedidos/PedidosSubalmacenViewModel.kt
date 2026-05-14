package mx.hmng.app.presentation.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.hmng.app.domain.model.PedidoSubalmacen
import mx.hmng.app.domain.repository.DetalleRequest
import mx.hmng.app.domain.repository.DetalleRespuesta
import mx.hmng.app.domain.repository.PedidoSubalmacenRepository
import javax.inject.Inject

sealed interface PedidosSubalmacenUiState {
    object Loading : PedidosSubalmacenUiState
    data class Success(val pedidos: List<PedidoSubalmacen>) : PedidosSubalmacenUiState
    data class Error(val message: String) : PedidosSubalmacenUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PedidosSubalmacenViewModel @Inject constructor(
    private val repo: PedidoSubalmacenRepository
) : ViewModel() {

    private val _estadoFilter = MutableStateFlow<String?>(null)
    val estadoFilter: StateFlow<String?> = _estadoFilter.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    val uiState: StateFlow<PedidosSubalmacenUiState> = _estadoFilter
        .flatMapLatest { estado -> repo.getPedidos(estado) }
        .catch { emit(emptyList()) }
        .map<List<PedidoSubalmacen>, PedidosSubalmacenUiState> { PedidosSubalmacenUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PedidosSubalmacenUiState.Loading
        )

    fun setFiltro(estado: String?) {
        _estadoFilter.value = estado
    }

    fun crearPedido(detalles: List<DetalleRequest>) {
        viewModelScope.launch {
            repo.crearPedido(detalles).onFailure { e ->
                _actionError.value = e.message ?: "Error al crear pedido"
            }
        }
    }

    fun atenderPedido(id: Int, detalles: List<DetalleRespuesta>) {
        viewModelScope.launch {
            repo.atenderPedido(id, detalles).onFailure { e ->
                _actionError.value = e.message ?: "Error al atender pedido"
            }
        }
    }

    fun cancelarPedido(id: Int) {
        viewModelScope.launch {
            repo.cancelarPedido(id).onFailure { e ->
                _actionError.value = e.message ?: "Error al cancelar pedido"
            }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }
}
