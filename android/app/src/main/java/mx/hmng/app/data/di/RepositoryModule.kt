package mx.hmng.app.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.hmng.app.data.repository.AuthRepositoryImpl
import mx.hmng.app.data.repository.BitacoraRepositoryImpl
import mx.hmng.app.data.repository.DashboardRepositoryImpl
import mx.hmng.app.data.repository.InsumoRepositoryImpl
import mx.hmng.app.data.repository.NotificacionRepositoryImpl
import mx.hmng.app.data.repository.PedidoRepositoryImpl
import mx.hmng.app.domain.repository.AuthRepository
import mx.hmng.app.domain.repository.BitacoraRepository
import mx.hmng.app.domain.repository.DashboardRepository
import mx.hmng.app.domain.repository.InsumoRepository
import mx.hmng.app.domain.repository.NotificacionRepository
import mx.hmng.app.domain.repository.PedidoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindInsumoRepository(impl: InsumoRepositoryImpl): InsumoRepository

    @Binds
    @Singleton
    abstract fun bindPedidoRepository(impl: PedidoRepositoryImpl): PedidoRepository

    @Binds
    @Singleton
    abstract fun bindNotificacionRepository(impl: NotificacionRepositoryImpl): NotificacionRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindBitacoraRepository(impl: BitacoraRepositoryImpl): BitacoraRepository
}
