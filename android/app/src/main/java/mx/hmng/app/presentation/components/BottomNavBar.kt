package mx.hmng.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import mx.hmng.app.presentation.navigation.NavRoutes

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val navItems = listOf(
    NavItem(NavRoutes.Dashboard.route, "Dashboard", Icons.Default.Dashboard),
    NavItem(NavRoutes.Insumos.route, "Insumos", Icons.Default.Inventory),
    NavItem(NavRoutes.PedidosSubalmacen.route, "Pedidos", Icons.Default.ShoppingCart),
    NavItem(NavRoutes.Bitacora.route, "Bitácora", Icons.Default.MenuBook),
    NavItem("more", "Más", Icons.Default.MoreHoriz)
)

@Composable
fun BottomNavBar(
    navController: NavHostController,
    currentRoute: String,
    onMoreClick: () -> Unit
) {
    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
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
