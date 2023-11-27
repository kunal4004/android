package za.co.woolworths.financial.services.android.shoptoggle.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import za.co.woolworths.financial.services.android.common.ResourcesProvider
import za.co.woolworths.financial.services.android.shoptoggle.data.repository.LearnMoreRepositoryImpl
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.LearnMoreRepository


@Module
@InstallIn(SingletonComponent::class)
class LearnMoreModule {

    @Provides
    fun provideLearnMoreRepository(resourcesProvider: ResourcesProvider): LearnMoreRepository {
        return LearnMoreRepositoryImpl(resourcesProvider)
    }
}