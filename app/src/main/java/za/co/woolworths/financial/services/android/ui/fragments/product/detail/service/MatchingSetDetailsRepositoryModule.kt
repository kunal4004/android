package za.co.woolworths.financial.services.android.ui.fragments.product.detail.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import za.co.woolworths.financial.services.android.models.network.ApiInterface

@Module
@InstallIn(ViewModelComponent::class)
object MatchingSetDetailsRepositoryModule {

    @ViewModelScoped
    @Provides
    fun provideMatchingSetRepository(apiInterface: ApiInterface) = MatchingSetRepositoryImpl(apiInterface) as MatchingSetRepository

}