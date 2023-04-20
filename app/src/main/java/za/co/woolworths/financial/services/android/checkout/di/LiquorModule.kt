package za.co.woolworths.financial.services.android.checkout.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.checkout.repository.LiquorRepository
import za.co.woolworths.financial.services.android.checkout.repository.LiquourRepositoryImpl


@Module
@InstallIn(ViewModelComponent::class)
abstract class LiquorModule {
    @Binds
    abstract fun bindLiquorRepository(liquourRepositoryImpl: LiquourRepositoryImpl): LiquorRepository
}