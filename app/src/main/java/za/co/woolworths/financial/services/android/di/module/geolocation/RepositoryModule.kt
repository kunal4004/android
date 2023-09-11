package za.co.woolworths.financial.services.android.di.module.geolocation

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import za.co.woolworths.financial.services.android.geolocation.network.repository.ConfirmAddressRepository
import za.co.woolworths.financial.services.android.geolocation.network.repository.ConfirmAddressRepositoryImp
import za.co.woolworths.financial.services.android.models.network.ApiInterface

/**
 * Created by Kunal Uttarwar on 08/09/23.
 */

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Provides
    fun provideConfirmAddressRepository(
        apiInterface: ApiInterface,
    ): ConfirmAddressRepository {
        return ConfirmAddressRepositoryImp(apiInterface)
    }

    @Provides
    fun provideApiService(retrofit: Retrofit): ApiInterface = retrofit.create(
        ApiInterface::class.java
    )
}
