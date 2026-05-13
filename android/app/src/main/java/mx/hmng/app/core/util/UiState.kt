package mx.hmng.app.core.util

data class UiState<out T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
) {
    val isSuccess get() = data != null && !isLoading
    val hasError get() = error != null
}
