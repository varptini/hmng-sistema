package mx.hmng.app.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.hmng.app.data.repository.BitacoraRepositoryImpl
import mx.hmng.app.data.repository.DashboardRepositoryImpl
import mx.hmng.app.data.repository.InsumoRepositoryImpl
import mx.hmng.app.data.repository.PedidoAlmacenRepositoryImpl
import mx.hmng.app.data.repository.PedidoSubalmacenRepositoryImpl
import mx.hmng.app.domain.repository.BitacoraRepository
import mx.hmng.app.domain.repository.DashboardRepository
import mx.hmng.app.domain.repository.InsumoRepository
import mx.hmng.app.domain.repository.PedidoAlmacenRepository
import mx.hmng.app.domain.repository.PedidoSubalmacenRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindInsumoRepository(impl: InsumoRepositoryImpl): InsumoRepository

    @Binds
    @Singleton
    abstract fun bindPedidoSubalmacenRepository(impl: PedidoSubalmacenRepositoryImpl): PedidoSubalmacenRepository

    @Binds
    @Singleton
    abstract fun bindPedidoAlmacenRepository(impl: PedidoAlmacenRepositoryImpl): PedidoAlmacenRepository

    @Binds
    @Singleton
    abstract fun bindBitacoraRepository(impl: BitacoraRepositoryImpl): BitacoraRepository
}
