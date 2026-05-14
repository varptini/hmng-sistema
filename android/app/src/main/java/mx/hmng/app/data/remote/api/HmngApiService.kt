package mx.hmng.app.data.remote.api

import mx.hmng.app.data.dto.BitacoraDto
import mx.hmng.app.data.dto.DashboardDto
import mx.hmng.app.data.dto.InsumoDto
import mx.hmng.app.data.dto.NotificacionDto
import mx.hmng.app.data.dto.PedidoAlmacenDto
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

    // --- Insumos ---
    @GET("insumos")
    suspend fun getInsumos(): Response<List<InsumoDto>>

    @POST("insumos")
    suspend fun createInsumo(@Body payload: Map<String, Any>): Response<InsumoDto>

    @PUT("insumos/{id}")
    suspend fun updateInsumo(@Path("id") id: Int, @Body payload: Map<String, Any>): Response<InsumoDto>

    @DELETE("insumos/{id}")
    suspend fun deleteInsumo(@Path("id") id: Int): Response<Void>

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

    // --- Dashboard ---
    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardDto>

    // --- Notificaciones ---
    @GET("notificaciones")
    suspend fun getNotificaciones(): Response<List<NotificacionDto>>

    // --- Pedidos Subalmacén ---
    @GET("pedidos-subalmacen")
    suspend fun getPedidosSubalmacen(
        @Query("estado") estado: String? = null
    ): Response<List<PedidoSubalmacenDto>>

    @POST("pedidos-subalmacen")
    suspend fun createPedidoSubalmacen(@Body payload: Map<String, Any>): Response<PedidoSubalmacenDto>

    @PUT("pedidos-subalmacen/{id}/atender")
    suspend fun atenderPedido(
        @Path("id") id: Int,
        @Body payload: Map<String, Any>
    ): Response<PedidoSubalmacenDto>

    @PUT("pedidos-subalmacen/{id}/cancelar")
    suspend fun cancelarPedido(@Path("id") id: Int): Response<PedidoSubalmacenDto>

    // --- Pedidos Almacén ---
    @GET("pedidos-almacen")
    suspend fun getPedidosAlmacen(
        @Query("estado") estado: String? = null
    ): Response<List<PedidoAlmacenDto>>

    @GET("pedidos-almacen/{id}")
    suspend fun getPedidoAlmacen(@Path("id") id: Int): Response<PedidoAlmacenDto>

    @POST("pedidos-almacen")
    suspend fun createPedidoAlmacen(@Body payload: Map<String, Any>): Response<PedidoAlmacenDto>

    @PUT("pedidos-almacen/{id}/estado")
    suspend fun updateEstadoPedidoAlmacen(
        @Path("id") id: Int,
        @Body payload: Map<String, Any?>
    ): Response<PedidoAlmacenDto>

    // --- Bitácora ---
    @GET("bitacora")
    suspend fun getBitacora(
        @Query("page") page: Int? = null,
        @Query("tipo") tipo: String? = null,
        @Query("insumo_id") insumoId: Int? = null,
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null
    ): Response<List<BitacoraDto>>

    // Keep legacy createPedido for backward compatibility
    @POST("pedidos-subalmacen")
    suspend fun createPedido(@Body payload: Map<String, Any>): Response<PedidoSubalmacenDto>
}
