package za.co.woolworths.financial.services.android.di.module.shop

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.data.repository.MyListRepositoryImpl
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.network.ApiInterface

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    @Provides
    fun bindsMyListRepository(apiInterface: ApiInterface): MyListRepository {
        return MyListRepositoryImpl(apiInterface)
    }
}
