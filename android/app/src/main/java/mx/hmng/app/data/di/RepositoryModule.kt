package mx.hmng.app.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.hmng.app.data.repository.DashboardRepositoryImpl
import mx.hmng.app.data.repository.InsumoRepositoryImpl
import mx.hmng.app.domain.repository.DashboardRepository
import mx.hmng.app.domain.repository.InsumoRepository
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
}
