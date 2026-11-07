package mx.hmng.app.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.hmng.app.data.repository.BitacoraRepositoryImpl
import mx.hmng.app.data.repository.AuthRepositoryImpl
import mx.hmng.app.data.repository.EntradaRepositoryImpl
import mx.hmng.app.data.repository.SalidaRepositoryImpl
import mx.hmng.app.data.repository.ServicioRepositoryImpl
import mx.hmng.app.data.repository.EmpleadoRepositoryImpl
import mx.hmng.app.data.repository.UsuarioRepositoryImpl
import mx.hmng.app.data.repository.RolRepositoryImpl
import mx.hmng.app.data.repository.DashboardRepositoryImpl
import mx.hmng.app.data.repository.InsumoRepositoryImpl
import mx.hmng.app.data.repository.NotificacionRepositoryImpl
import mx.hmng.app.data.repository.PedidoAlmacenRepositoryImpl
import mx.hmng.app.data.repository.PedidoSubalmacenRepositoryImpl
import mx.hmng.app.data.repository.ReportesRepositoryImpl
import mx.hmng.app.domain.repository.BitacoraRepository
import mx.hmng.app.domain.repository.AuthRepository
import mx.hmng.app.domain.repository.EntradaRepository
import mx.hmng.app.domain.repository.SalidaRepository
import mx.hmng.app.domain.repository.ServicioRepository
import mx.hmng.app.domain.repository.EmpleadoRepository
import mx.hmng.app.domain.repository.UsuarioRepository
import mx.hmng.app.domain.repository.RolRepository
import mx.hmng.app.domain.repository.DashboardRepository
import mx.hmng.app.domain.repository.InsumoRepository
import mx.hmng.app.domain.repository.NotificacionRepository
import mx.hmng.app.domain.repository.PedidoAlmacenRepository
import mx.hmng.app.domain.repository.PedidoSubalmacenRepository
import mx.hmng.app.domain.repository.ReportesRepository
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

    @Binds
    @Singleton
    abstract fun bindNotificacionRepository(impl: NotificacionRepositoryImpl): NotificacionRepository

    @Binds
    @Singleton
    abstract fun bindReportesRepository(impl: ReportesRepositoryImpl): ReportesRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindEntradaRepository(impl: EntradaRepositoryImpl): EntradaRepository

    @Binds
    @Singleton
    abstract fun bindSalidaRepository(impl: SalidaRepositoryImpl): SalidaRepository

    @Binds
    @Singleton
    abstract fun bindServicioRepository(impl: ServicioRepositoryImpl): ServicioRepository

    @Binds
    @Singleton
    abstract fun bindEmpleadoRepository(impl: EmpleadoRepositoryImpl): EmpleadoRepository

    @Binds
    @Singleton
    abstract fun bindUsuarioRepository(impl: UsuarioRepositoryImpl): UsuarioRepository

    @Binds
    @Singleton
    abstract fun bindRolRepository(impl: RolRepositoryImpl): RolRepository
}
