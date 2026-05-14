package mx.hmng.app.data.remote.api

import mx.hmng.app.data.dto.AuthResponseDto
import mx.hmng.app.data.dto.BitacoraDto
import mx.hmng.app.data.dto.DashboardDto
import mx.hmng.app.data.dto.InsumoDto
import mx.hmng.app.data.dto.NotificacionDto
import mx.hmng.app.data.dto.PedidoSubalmacenDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HmngApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body payload: Map<String, String>): Response<AuthResponseDto>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/cambiar-contrasena")
    suspend fun cambiarContrasena(@Body payload: Map<String, String>): Response<Unit>

    // Insumos
    @GET("insumos")
    suspend fun getInsumos(
        @Query("search") search: String? = null,
        @Query("alerta") alerta: Boolean? = null,
        @Query("page") page: Int? = null
    ): Response<List<InsumoDto>>

    @POST("insumos")
    suspend fun createInsumo(@Body payload: Map<String, Any>): Response<InsumoDto>

    @PUT("insumos/{id}")
    suspend fun updateInsumo(
        @Path("id") id: Int,
        @Body payload: Map<String, Any>
    ): Response<InsumoDto>

    @DELETE("insumos/{id}")
    suspend fun deleteInsumo(@Path("id") id: Int): Response<Unit>

    @POST("insumos/{id}/entrada")
    suspend fun createEntrada(
        @Path("id") insumoId: Int,
        @Body payload: Map<String, Any>
    ): Response<InsumoDto>

    @POST("insumos/{id}/salida")
    suspend fun createSalida(
        @Path("id") insumoId: Int,
        @Body payload: Map<String, Any>
    ): Response<InsumoDto>

    // Notificaciones
    @GET("notificaciones")
    suspend fun getNotificaciones(): Response<List<NotificacionDto>>

    @POST("notificaciones/marcar-leidas")
    suspend fun marcarNotificacionesLeidas(): Response<Unit>

    // Dashboard
    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardDto>

    // Pedidos subalmacén
    @GET("pedidos-subalmacen")
    suspend fun getPedidosSubalmacen(): Response<List<PedidoSubalmacenDto>>

    @POST("pedidos-subalmacen")
    suspend fun createPedido(@Body payload: Map<String, Any>): Response<PedidoSubalmacenDto>

    @POST("pedidos-subalmacen/{id}/atender")
    suspend fun atenderPedido(@Path("id") id: Int): Response<PedidoSubalmacenDto>

    @POST("pedidos-subalmacen/{id}/cancelar")
    suspend fun cancelarPedido(@Path("id") id: Int): Response<PedidoSubalmacenDto>

    // Pedidos almacén
    @GET("pedidos-almacen")
    suspend fun getPedidosAlmacen(): Response<List<PedidoSubalmacenDto>>

    @PUT("pedidos-almacen/{id}/estado")
    suspend fun updateEstadoPedidoAlmacen(
        @Path("id") id: Int,
        @Body payload: Map<String, Any>
    ): Response<PedidoSubalmacenDto>

    // Bitácora
    @GET("bitacora")
    suspend fun getBitacora(): Response<List<BitacoraDto>>
}
