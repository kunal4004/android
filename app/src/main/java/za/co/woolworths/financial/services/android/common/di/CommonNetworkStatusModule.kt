package za.co.woolworths.financial.services.android.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import za.co.woolworths.financial.services.android.common.CommonConnectivityStatus
import za.co.woolworths.financial.services.android.common.CommonConnectivityStatusImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class CommonNetworkStatusModule {

    @Binds
    abstract fun bindCommonNetworkStatus(commonConnectivityStatusImpl: CommonConnectivityStatusImpl): CommonConnectivityStatus

}