package mx.hmng.app.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mx.hmng.app.data.local.HmngDatabase
import mx.hmng.app.data.local.dao.BitacoraDao
import mx.hmng.app.data.local.dao.DashboardCacheDao
import mx.hmng.app.data.local.dao.InsumoDao
import mx.hmng.app.data.local.dao.NotificacionDao
import mx.hmng.app.data.local.dao.PedidoSubalmacenDao
import mx.hmng.app.data.local.dao.PendingOperationDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HmngDatabase =
        Room.databaseBuilder(
            context,
            HmngDatabase::class.java,
            "hmng.db"
        ).build()

    @Provides
    fun provideInsumoDao(db: HmngDatabase): InsumoDao = db.insumoDao()

    @Provides
    fun provideNotificacionDao(db: HmngDatabase): NotificacionDao = db.notificacionDao()

    @Provides
    fun provideDashboardCacheDao(db: HmngDatabase): DashboardCacheDao = db.dashboardCacheDao()

    @Provides
    fun providePedidoSubalmacenDao(db: HmngDatabase): PedidoSubalmacenDao = db.pedidoSubalmacenDao()

    @Provides
    fun provideBitacoraDao(db: HmngDatabase): BitacoraDao = db.bitacoraDao()

    @Provides
    fun providePendingOperationDao(db: HmngDatabase): PendingOperationDao = db.pendingOperationDao()
}
