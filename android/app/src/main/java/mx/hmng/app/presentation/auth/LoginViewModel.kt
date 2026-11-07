package mx.hmng.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.hmng.app.core.util.UiState
import mx.hmng.app.core.util.userMessage
import mx.hmng.app.domain.model.Usuario
import mx.hmng.app.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState<Usuario>())
    val uiState: StateFlow<UiState<Usuario>> = _uiState.asStateFlow()

    fun login(usuario: String, contrasena: String) {
        if (_uiState.value.isLoading) return
        if (usuario.isBlank() || contrasena.isBlank()) {
            _uiState.value = UiState(error = "Ingresa usuario y contraseña")
            return
        }
        _uiState.value = UiState(isLoading = true)
        viewModelScope.launch {
            authRepository.login(usuario.trim(), contrasena)
                .onSuccess { _uiState.value = UiState(data = it) }
                .onFailure { e ->
                    _uiState.value = UiState(error = e.userMessage("No se pudo iniciar sesión"))
                }
        }
    }

    fun clearError() {
        if (_uiState.value.hasError) _uiState.value = UiState()
    }
}
