package za.co.woolworths.financial.services.android.di.module.shop

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.data.repository.CheckoutRepositoryImpl
import za.co.woolworths.financial.services.android.data.repository.MyListRepositoryImpl
import za.co.woolworths.financial.services.android.domain.repository.CheckoutRepository
import za.co.woolworths.financial.services.android.data.repository.OrderAgainRepositoryImpl
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.domain.repository.OrderAgainRepository
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.data.repository.NotifyMeRepositoryImpl
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.domain.repository.NotifyMeRepository

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindsNotifyMeRepository(impl: NotifyMeRepositoryImpl): NotifyMeRepository

    @Binds
    abstract fun bindsMyListRepository(impl: MyListRepositoryImpl): MyListRepository

    @Binds
    abstract fun bindsCheckoutRepository(impl: CheckoutRepositoryImpl): CheckoutRepository

    @Binds
    abstract fun bindsOrderAgainRepository(impl: OrderAgainRepositoryImpl): OrderAgainRepository
}
