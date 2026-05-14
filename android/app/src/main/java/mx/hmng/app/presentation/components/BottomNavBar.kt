package mx.hmng.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import mx.hmng.app.presentation.navigation.NavRoutes

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
    val roleRequired: List<String> = emptyList()
)

@Composable
fun BottomNavBar(
    navController: NavHostController,
    currentRoute: String,
    userRole: String = "",
    unreadCount: Int = 0,
    onMoreClick: () -> Unit
) {
    val pedidosRoute = when (userRole.uppercase()) {
        "SUBALMACEN" -> NavRoutes.PedidosSubalmacen.route
        else -> NavRoutes.PedidosAlmacen.route
    }

    val items = buildList {
        add(NavItem(NavRoutes.Dashboard.route, "Dashboard", Icons.Default.Dashboard))
        add(NavItem(NavRoutes.Insumos.route, "Insumos", Icons.Default.Inventory))
        add(NavItem(pedidosRoute, "Pedidos", Icons.Default.ShoppingCart))
        add(NavItem(NavRoutes.Bitacora.route, "Bitácora", Icons.Default.History))
        add(NavItem(NavRoutes.Notificaciones.route, "Avisos", Icons.Default.Notifications, badgeCount = unreadCount))
        if (userRole.uppercase() in listOf("ADMIN", "ALMACENISTA", "ALMACEN")) {
            add(NavItem(NavRoutes.Reportes.route, "Reportes", Icons.Default.BarChart))
        } else {
            add(NavItem("more", "Más", Icons.Default.MoreHoriz))
        }
    }

    NavigationBar {
        items.forEach { item ->
            val isSelected = currentRoute == item.route ||
                (item.route == pedidosRoute &&
                    (currentRoute == NavRoutes.PedidosSubalmacen.route ||
                        currentRoute == NavRoutes.PedidosAlmacen.route))

            NavigationBarItem(
                icon = {
                    if (item.badgeCount > 0) {
                        BadgedBox(badge = { Badge { Text(item.badgeCount.toString()) } }) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = {
                    if (item.route == "more") {
                        onMoreClick()
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(NavRoutes.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
