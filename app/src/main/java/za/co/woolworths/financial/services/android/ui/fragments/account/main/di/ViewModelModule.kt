package za.co.woolworths.financial.services.android.ui.fragments.account.main.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.ITreatmentPlanDataSource
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.TreatmentPlanDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.StoreCardRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.ManageCardFunctionalRequirementImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.CreditCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.CreditCardService

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun provideStoreCardService(retrofit: Retrofit): WfsApiService = retrofit.create(
        WfsApiService::class.java
    )

//    @Provides
//    fun provideCLIFirebaseEvent(): CreditLimitIncreaseFirebaseEvent = CreditLimitIncreaseFirebaseEvent()
//
    @Provides
    fun provideStoreCardDataSource(
    wfsApiService: WfsApiService,
    accountProductLandingDao: AccountProductLandingDao,
    manageCardFunctionalRequirementImpl: ManageCardFunctionalRequirementImpl,
    ) = StoreCardDataSource(wfsApiService, accountProductLandingDao,manageCardFunctionalRequirementImpl)

    @Provides
    fun provideRepository(remoteDataSource: StoreCardDataSource) =
        StoreCardRepository(remoteDataSource)


    @Provides
    fun provideCreditCardRemoteService(retrofit: Retrofit): CreditCardService =
        retrofit.create(CreditCardService::class.java)


    @Provides
    fun provideCreditCardDataSource(creditCardService: CreditCardService): CreditCardDataSource =
        CreditCardDataSource(creditCardService)

    @Provides
    fun provideTreatmentPlanDataSource(): ITreatmentPlanDataSource {
        return TreatmentPlanDataSource()
    }

}