package mx.hmng.app.data.remote.api

import mx.hmng.app.data.dto.BitacoraDto
import mx.hmng.app.data.dto.DashboardDto
import mx.hmng.app.data.dto.InsumoDto
import mx.hmng.app.data.dto.NotificacionDto
import mx.hmng.app.data.dto.PedidoSubalmacenDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HmngApiService {

    @GET("insumos")
    suspend fun getInsumos(): Response<List<InsumoDto>>

    @GET("notificaciones")
    suspend fun getNotificaciones(): Response<List<NotificacionDto>>

    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardDto>

    @GET("pedidos-subalmacen")
    suspend fun getPedidosSubalmacen(): Response<List<PedidoSubalmacenDto>>

    @GET("bitacora")
    suspend fun getBitacora(): Response<List<BitacoraDto>>

    @POST("pedidos-subalmacen")
    suspend fun createPedido(@Body payload: Map<String, Any>): Response<PedidoSubalmacenDto>

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
}
