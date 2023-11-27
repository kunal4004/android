package za.co.woolworths.financial.services.android.shoptoggle.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.shoptoggle.data.repository.ShopToggleRepositoryImpl
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.ShopToggleRepository


@Module
@InstallIn(SingletonComponent::class)
class ShopToggleModule {

    @Provides
    fun provideShopToggleRepository(geoLocationApiHelper: GeoLocationApiHelper): ShopToggleRepository {
        return ShopToggleRepositoryImpl(geoLocationApiHelper)
    }
}