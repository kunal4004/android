package za.co.woolworths.financial.services.android.ui.fragments.account.main.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.collection.CollectionRemoteDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.CollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.StoreCardRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.collection.CollectionRemoteApiService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.CreditCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.CreditCardService

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountProductsModule {

    @Provides
    fun provideStoreCardService(retrofit: Retrofit): StoreCardService = retrofit.create(
        StoreCardService::class.java)

    @Singleton
    @Provides
    fun provideStoreCardDataSource(storeCardService: StoreCardService) =
        StoreCardDataSource(storeCardService)

    @Singleton
    @Provides
    fun provideRepository(remoteDataSource: StoreCardDataSource) =
        StoreCardRepository(remoteDataSource)


    @Singleton
    @Provides
    fun provideCollectionRemoteDataSource(collectionRemoteApiService: CollectionRemoteApiService) =
        CollectionRemoteDataSource(collectionRemoteApiService)

    @Singleton
    @Provides
    fun provideCollectionRepository(remoteDataSource: CollectionRemoteDataSource) =
        CollectionRepository(remoteDataSource)

    @Provides
    fun provideCollectionRemoteApiService(retrofit: Retrofit): CollectionRemoteApiService = retrofit.create(
        CollectionRemoteApiService::class.java)

    @Provides
    fun provideCreditCardRemoteService(retrofit: Retrofit): CreditCardService = retrofit.create(CreditCardService::class.java)


    @Provides
    fun provideCreditCardDataSource(creditCardService: CreditCardService): CreditCardDataSource = CreditCardDataSource(creditCardService)

}