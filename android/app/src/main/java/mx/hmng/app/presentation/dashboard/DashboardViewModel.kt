package mx.hmng.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import mx.hmng.app.domain.model.StockCritico
import mx.hmng.app.domain.model.Tendencia
import mx.hmng.app.domain.repository.DashboardRepository
import mx.hmng.app.domain.usecase.GetCurrentUserUseCase
import javax.inject.Inject

data class ResumenDashboard(
    val totalInsumos: Int = 0,
    val porCaducar: Int = 0,
    val caducados: Int = 0,
    val stockBajo: Int = 0,
    val pedidosPendientes: Int = 0,
    val entradasMes: Int = 0,
    val salidasMes: Int = 0,
    val pedidosAlmacen: Int = 0
)

data class MovimientoReciente(
    val id: Int = 0,
    val tipo: String = "",
    val insumo: String = "",
    val cantidad: Double = 0.0,
    val fecha: String = "",
    val usuario: String = ""
)

typealias StockCriticoItem = StockCritico
typealias TendenciaDia = Tendencia

data class DashboardUiState(
    val isLoading: Boolean = true,
    val resumen: ResumenDashboard? = null,
    val stockCritico: List<StockCriticoItem> = emptyList(),
    val tendencia: List<TendenciaDia> = emptyList(),
    val movimientosRecientes: List<MovimientoReciente> = emptyList(),
    val error: String? = null,
    val userName: String = ""
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepo: DashboardRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        val userName = getCurrentUserUseCase()?.nombre ?: ""
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, userName = userName)

        dashboardRepo.getDashboard()
            .onEach { dashboard ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    resumen = ResumenDashboard(
                        totalInsumos = dashboard.resumen.totalInsumos,
                        caducados = dashboard.resumen.caducados,
                        stockBajo = dashboard.resumen.stockBajo,
                        pedidosPendientes = dashboard.resumen.pedidosPendientes
                    ),
                    stockCritico = dashboard.stockCritico,
                    tendencia = dashboard.tendencias
                )
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar el dashboard"
                )
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            dashboardRepo.refreshDashboard()
                .onSuccess { dashboard ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resumen = ResumenDashboard(
                            totalInsumos = dashboard.resumen.totalInsumos,
                            caducados = dashboard.resumen.caducados,
                            stockBajo = dashboard.resumen.stockBajo,
                            pedidosPendientes = dashboard.resumen.pedidosPendientes
                        ),
                        stockCritico = dashboard.stockCritico,
                        tendencia = dashboard.tendencias
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al actualizar"
                    )
                }
        }
    }
}
