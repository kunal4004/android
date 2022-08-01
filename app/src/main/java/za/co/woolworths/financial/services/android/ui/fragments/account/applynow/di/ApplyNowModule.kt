package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.ApplyNowBottomSheetImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data.ApplyNowApiService
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data.ApplyNowRemoteDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data.ApplyNowRepo

@Module
@InstallIn(ViewModelComponent::class)
object ApplyNowModule {

    @Provides
    fun provideApplyNowBottomSheetDao() = ApplyNowBottomSheetImpl()


    @Provides
    fun provideApplyNowService(retrofit: Retrofit): ApplyNowApiService = retrofit.create(
        ApplyNowApiService::class.java
    )
    @Provides
    fun provideReemoteDataSourcey(service: ApplyNowApiService) = ApplyNowRemoteDataSource(service)

    @Provides
    fun provideRepository(remoteDataSource: ApplyNowRemoteDataSource) = ApplyNowRepo(remoteDataSource)

}