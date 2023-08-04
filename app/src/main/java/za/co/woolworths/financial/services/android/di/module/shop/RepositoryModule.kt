package za.co.woolworths.financial.services.android.di.module.shop

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.data.repository.MyListRepositoryImpl
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindsMyListRepository(impl: MyListRepositoryImpl): MyListRepository
}