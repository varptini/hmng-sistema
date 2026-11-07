package mx.hmng.app.data.remote.api

import mx.hmng.app.data.dto.BitacoraDto
import mx.hmng.app.data.dto.CreatedResponseDto
import mx.hmng.app.data.dto.EmpleadoDto
import mx.hmng.app.data.dto.EntradaListResponseDto
import mx.hmng.app.data.dto.LoginResponseDto
import mx.hmng.app.data.dto.MessageResponseDto
import mx.hmng.app.data.dto.RolDto
import mx.hmng.app.data.dto.SalidaListResponseDto
import mx.hmng.app.data.dto.ServicioDto
import mx.hmng.app.data.dto.UsuarioSistemaDto
import mx.hmng.app.data.dto.DashboardDto
import mx.hmng.app.data.dto.InsumoDto
import mx.hmng.app.data.dto.InsumosPageDto
import mx.hmng.app.data.dto.NotificacionDto
import mx.hmng.app.data.dto.PedidoAlmacenDto
import mx.hmng.app.data.dto.PedidoSubalmacenDto
import mx.hmng.app.data.dto.InsumoInventarioDto
import mx.hmng.app.data.dto.MovimientoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HmngApiService {

    // --- Auth ---
    @POST("auth/login")
    suspend fun login(@Body payload: Map<String, String>): Response<LoginResponseDto>

    // --- Insumos ---
    // GET /insumos devuelve { data, total, page, limit }, no un array plano.
    @GET("insumos")
    suspend fun getInsumos(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<InsumosPageDto>

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

    @PUT("notificaciones/leidas")
    suspend fun marcarNotificacionesLeidas(@Body payload: Map<String, Any>): Response<Void>

    // --- Reportes ---
    @GET("reportes/inventario")
    suspend fun getInventario(): Response<List<InsumoInventarioDto>>

    @GET("reportes/movimientos")
    suspend fun getMovimientos(
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null,
        @Query("tipo") tipo: String? = null
    ): Response<List<MovimientoDto>>

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

    // --- Entradas ---
    @GET("entradas")
    suspend fun getEntradas(): Response<EntradaListResponseDto>

    @POST("entradas")
    suspend fun createEntradaGeneral(@Body payload: Map<String, Any>): Response<CreatedResponseDto>

    // --- Salidas ---
    @GET("salidas")
    suspend fun getSalidas(): Response<SalidaListResponseDto>

    @POST("salidas")
    suspend fun createSalidaGeneral(@Body payload: Map<String, Any>): Response<CreatedResponseDto>

    // --- Servicios ---
    @GET("servicios")
    suspend fun getServicios(): Response<List<ServicioDto>>

    @POST("servicios")
    suspend fun createServicio(@Body payload: Map<String, Any>): Response<ServicioDto>

    @PUT("servicios/{id}")
    suspend fun updateServicio(@Path("id") id: Int, @Body payload: Map<String, Any>): Response<ServicioDto>

    // --- Empleados ---
    @GET("empleados")
    suspend fun getEmpleados(): Response<List<EmpleadoDto>>

    @POST("empleados")
    suspend fun createEmpleado(@Body payload: Map<String, Any>): Response<EmpleadoDto>

    @PUT("empleados/{id}")
    suspend fun updateEmpleado(@Path("id") id: Int, @Body payload: Map<String, Any>): Response<EmpleadoDto>

    @DELETE("empleados/{id}")
    suspend fun deleteEmpleado(@Path("id") id: Int): Response<MessageResponseDto>

    // --- Usuarios ---
    @GET("usuarios")
    suspend fun getUsuarios(): Response<List<UsuarioSistemaDto>>

    @POST("usuarios")
    suspend fun createUsuario(@Body payload: Map<String, Any>): Response<UsuarioSistemaDto>

    @PUT("usuarios/{id}")
    suspend fun updateUsuario(@Path("id") id: Int, @Body payload: Map<String, Any>): Response<UsuarioSistemaDto>

    @DELETE("usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Int): Response<MessageResponseDto>

    // --- Roles ---
    @GET("roles")
    suspend fun getRoles(): Response<List<RolDto>>

    @POST("roles")
    suspend fun createRol(@Body payload: Map<String, Any>): Response<RolDto>

    @PUT("roles/{id}")
    suspend fun updateRol(@Path("id") id: Int, @Body payload: Map<String, Any>): Response<RolDto>

    @DELETE("roles/{id}")
    suspend fun deleteRol(@Path("id") id: Int): Response<Void>

    // Keep legacy createPedido for backward compatibility
    @POST("pedidos-subalmacen")
    suspend fun createPedido(@Body payload: Map<String, Any>): Response<PedidoSubalmacenDto>
}
