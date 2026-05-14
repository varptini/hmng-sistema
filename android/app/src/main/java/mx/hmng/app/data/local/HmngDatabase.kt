package mx.hmng.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import mx.hmng.app.data.local.dao.BitacoraDao
import mx.hmng.app.data.local.dao.DashboardCacheDao
import mx.hmng.app.data.local.dao.InsumoDao
import mx.hmng.app.data.local.dao.NotificacionDao
import mx.hmng.app.data.local.dao.PedidoSubalmacenDao
import mx.hmng.app.data.local.dao.PendingOperationDao
import mx.hmng.app.data.local.entity.BitacoraEntity
import mx.hmng.app.data.local.entity.DashboardCacheEntity
import mx.hmng.app.data.local.entity.InsumoEntity
import mx.hmng.app.data.local.entity.NotificacionEntity
import mx.hmng.app.data.local.entity.PedidoSubalmacenEntity
import mx.hmng.app.data.local.entity.PendingOperationEntity

@Database(
    entities = [
        InsumoEntity::class,
        NotificacionEntity::class,
        DashboardCacheEntity::class,
        PedidoSubalmacenEntity::class,
        BitacoraEntity::class,
        PendingOperationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HmngDatabase : RoomDatabase() {
    abstract fun insumoDao(): InsumoDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun dashboardCacheDao(): DashboardCacheDao
    abstract fun pedidoSubalmacenDao(): PedidoSubalmacenDao
    abstract fun bitacoraDao(): BitacoraDao
    abstract fun pendingOperationDao(): PendingOperationDao
}
