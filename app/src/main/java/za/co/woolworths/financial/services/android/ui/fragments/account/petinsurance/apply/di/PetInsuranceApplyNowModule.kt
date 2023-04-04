package za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply.data.PetInsuranceApplyNowRemoteDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply.data.PetInsuranceApplyNowRepo
import za.co.woolworths.financial.services.android.util.Utils

@Module
@InstallIn(ViewModelComponent::class)
object PetInsuranceApplyNowModule {
    @Provides
    fun provideRepository(remoteDataSource: PetInsuranceApplyNowRemoteDataSource) = PetInsuranceApplyNowRepo(remoteDataSource)
}