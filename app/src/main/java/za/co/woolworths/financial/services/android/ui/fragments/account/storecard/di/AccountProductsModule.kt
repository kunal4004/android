package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote.StoreCardService
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.repository.StoreCardRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountProductsModule {

    @Provides
    fun provideStoreCardService(retrofit: Retrofit): StoreCardService = retrofit.create(StoreCardService::class.java)

    @Singleton
    @Provides
    fun provideStoreCardDataSource(storeCardService: StoreCardService) =
        StoreCardDataSource(storeCardService)


    @Singleton
    @Provides
    fun provideRepository(remoteDataSource: StoreCardDataSource) =
        StoreCardRepository(remoteDataSource)
}