package za.co.woolworths.financial.services.android.di.module.shop

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import za.co.woolworths.financial.services.android.repository.shop.MainShopRepository
import za.co.woolworths.financial.services.android.repository.shop.ShopRepository

@Module
@InstallIn(ViewModelComponent::class)
object ShopRepositoryModule {

    @ViewModelScoped
    @Provides
    fun provideMainShopRepository() = MainShopRepository() as ShopRepository
}