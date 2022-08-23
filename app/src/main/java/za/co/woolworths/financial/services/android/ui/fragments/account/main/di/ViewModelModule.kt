package za.co.woolworths.financial.services.android.ui.fragments.account.main.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.TreatmentPlanDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.StoreCardRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.ManageCardFunctionalRequirementImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.CreditCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.CreditCardService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.feature_pay_my_account.PaymentsPayuMethodsDataSource

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun provideStoreCardService(retrofit: Retrofit): WfsApiService = retrofit.create(
        WfsApiService::class.java
    )

    @Provides
    fun provideStoreCardDataSource(
    wfsApiService: WfsApiService,
    accountProductLandingDao: AccountProductLandingDao,
    manageCardFunctionalRequirementImpl: ManageCardFunctionalRequirementImpl,
    ) = StoreCardDataSource(wfsApiService, accountProductLandingDao,manageCardFunctionalRequirementImpl)

    @Provides
    fun provideRepository(remoteDataSource: StoreCardDataSource) = StoreCardRepository(remoteDataSource)

    @Provides
    fun provideCreditCardRemoteService(retrofit: Retrofit) = retrofit.create(CreditCardService::class.java)

    @Provides
    fun provideCreditCardDataSource(creditCardService: CreditCardService) = CreditCardDataSource(creditCardService)

    @Provides
    fun provideTreatmentPlanDataSource() = TreatmentPlanDataSource()

    @Provides
    fun provideAccountProductLandingDao() = AccountProductLandingDao()

    @Provides
    fun providePaymentsPayuMethodsDataSource(remoteDataSource: WfsApiService) = PaymentsPayuMethodsDataSource(remoteDataSource)


}