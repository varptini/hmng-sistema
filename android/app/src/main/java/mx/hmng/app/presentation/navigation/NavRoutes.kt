package mx.hmng.app.presentation.navigation

sealed class NavRoutes(val route: String) {
    object Login : NavRoutes("login")
    object Dashboard : NavRoutes("dashboard")
    object Insumos : NavRoutes("insumos")
    object Entradas : NavRoutes("entradas")
    object PedidosSubalmacen : NavRoutes("pedidos_subalmacen")
    object PedidosAlmacen : NavRoutes("pedidos_almacen")
    object Bitacora : NavRoutes("bitacora")
    object Notificaciones : NavRoutes("notificaciones")
    object Reportes : NavRoutes("reportes")
}
