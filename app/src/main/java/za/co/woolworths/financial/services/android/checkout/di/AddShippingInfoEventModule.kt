package za.co.woolworths.financial.services.android.checkout.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import za.co.woolworths.financial.services.android.checkout.utils.AddShippingInfoEventsAnalytics
import za.co.woolworths.financial.services.android.checkout.utils.AddShippingInfoEventsAnalyticsImpl


@Module
@InstallIn(ActivityComponent::class)
abstract class AddShippingInfoEventModule {
    @Binds
    abstract fun bindAddShippingInfoEventModule(addShippingInfoEventsAnalyticsImpl: AddShippingInfoEventsAnalyticsImpl): AddShippingInfoEventsAnalytics

}