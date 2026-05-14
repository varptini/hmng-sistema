package mx.hmng.app.data.socket

import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import mx.hmng.app.BuildConfig
import mx.hmng.app.data.dto.InsumoDto
import mx.hmng.app.data.dto.NotificacionDto
import mx.hmng.app.data.session.SessionManager
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val sessionManager: SessionManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()
    private var socket: Socket? = null

    private val _notificacionFlow = MutableSharedFlow<NotificacionDto>(extraBufferCapacity = 64)
    val notificacionFlow: SharedFlow<NotificacionDto> = _notificacionFlow.asSharedFlow()

    private val _stockMinimoFlow = MutableSharedFlow<InsumoDto>(extraBufferCapacity = 64)
    val stockMinimoFlow: SharedFlow<InsumoDto> = _stockMinimoFlow.asSharedFlow()

    private val _pedidoNuevoFlow = MutableSharedFlow<Int>(extraBufferCapacity = 64)
    val pedidoNuevoFlow: SharedFlow<Int> = _pedidoNuevoFlow.asSharedFlow()

    private val _pedidoAtendidoFlow = MutableSharedFlow<Int>(extraBufferCapacity = 64)
    val pedidoAtendidoFlow: SharedFlow<Int> = _pedidoAtendidoFlow.asSharedFlow()

    fun connect() {
        val token = sessionManager.getUser()?.token ?: return
        disconnect()
        val opts = IO.Options().apply {
            auth = mapOf("token" to token)
        }
        socket = IO.socket(BuildConfig.SOCKET_URL, opts).also { s ->
            s.on("notificacion_nueva") { args ->
                val json = args.getOrNull(0) as? JSONObject ?: return@on
                runCatching { gson.fromJson(json.toString(), NotificacionDto::class.java) }
                    .onSuccess { scope.launch { _notificacionFlow.emit(it) } }
            }
            s.on("stock_minimo") { args ->
                val json = args.getOrNull(0) as? JSONObject ?: return@on
                runCatching { gson.fromJson(json.toString(), InsumoDto::class.java) }
                    .onSuccess { scope.launch { _stockMinimoFlow.emit(it) } }
            }
            s.on("pedido_nuevo") { args ->
                val id = (args.getOrNull(0) as? Number)?.toInt() ?: return@on
                scope.launch { _pedidoNuevoFlow.emit(id) }
            }
            s.on("pedido_atendido") { args ->
                val id = (args.getOrNull(0) as? Number)?.toInt() ?: return@on
                scope.launch { _pedidoAtendidoFlow.emit(id) }
            }
            s.connect()
        }
    }

    fun disconnect() {
        socket?.run {
            off()
            disconnect()
        }
        socket = null
    }
}
