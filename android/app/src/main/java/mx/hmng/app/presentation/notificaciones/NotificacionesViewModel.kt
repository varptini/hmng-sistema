package mx.hmng.app.presentation.notificaciones

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
import mx.hmng.app.data.socket.SocketManager
import mx.hmng.app.domain.model.Notificacion
import mx.hmng.app.domain.repository.NotificacionRepository
import javax.inject.Inject

@HiltViewModel
class NotificacionesViewModel @Inject constructor(
    private val repository: NotificacionRepository,
    val socketManager: SocketManager
) : ViewModel() {

    private val _notificaciones = MutableStateFlow<List<Notificacion>>(emptyList())
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        repository.getNotificaciones()
            .onEach { _notificaciones.value = it }
            .catch { }
            .launchIn(viewModelScope)

        repository.getUnreadCount()
            .onEach { _unreadCount.value = it }
            .catch { }
            .launchIn(viewModelScope)

        fetch()
    }

    fun fetch() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchAndStore()
            _isRefreshing.value = false
        }
    }

    fun marcarTodasLeidas() {
        viewModelScope.launch { repository.marcarTodasLeidas() }
    }
}
