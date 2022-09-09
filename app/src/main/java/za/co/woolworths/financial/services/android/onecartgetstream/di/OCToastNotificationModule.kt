package za.co.woolworths.financial.services.android.onecartgetstream.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.onecartgetstream.repository.OCToastNotification
import za.co.woolworths.financial.services.android.onecartgetstream.repository.OCToastNotificationImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class OCToastNotificationModule {

    @Binds
    abstract fun bindOCToastNotification(ocToastNotificationImpl: OCToastNotificationImpl): OCToastNotification
}
