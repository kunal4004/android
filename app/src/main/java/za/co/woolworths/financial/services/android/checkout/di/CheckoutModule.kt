package za.co.woolworths.financial.services.android.checkout.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.checkout.repository.CheckoutLiquorRepository
import za.co.woolworths.financial.services.android.checkout.repository.CheckoutLiquorRepositoryImpl


@Module
@InstallIn(ViewModelComponent::class)
abstract class CheckoutModule {
    @Binds
    abstract fun bindLiquorRepository(liquorRepositoryImpl: CheckoutLiquorRepositoryImpl): CheckoutLiquorRepository
}