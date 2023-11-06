package za.co.woolworths.financial.services.android.ui.activities.write_a_review.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.repository.WriteAReviewFormRepository
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.repository.WriteAReviewFormRepositoryImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class WriteAReviewFormModule {
    @Binds
    abstract fun bindWriteAReviewModule(writeAReviewFormRepositoryImpl: WriteAReviewFormRepositoryImpl): WriteAReviewFormRepository
}