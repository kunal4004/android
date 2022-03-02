package za.co.woolworths.financial.services.android.ui.fragments.account.main.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.StoreCardRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountProductsModule {

    @Provides
    fun provideStoreCardService(retrofit: Retrofit): StoreCardService = retrofit.create(
        StoreCardService::class.java)

    @Singleton
    @Provides
    fun provideStoreCardDataSource(storeCardService: StoreCardService, networkConfig: NetworkConfig) =
        StoreCardDataSource(storeCardService, networkConfig)

    @Singleton
    @Provides
    fun provideNetworkConfig() = NetworkConfig()

    @Singleton
    @Provides
    fun provideRepository(remoteDataSource: StoreCardDataSource) =
        StoreCardRepository(remoteDataSource)
}