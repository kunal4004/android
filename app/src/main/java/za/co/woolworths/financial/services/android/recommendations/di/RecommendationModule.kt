package za.co.woolworths.financial.services.android.recommendations.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepositoryImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class RecommendationModule {

    @Binds
    abstract fun bindRecommendationModule(recommendationsRepositoryImpl: RecommendationsRepositoryImpl): RecommendationsRepository
}
