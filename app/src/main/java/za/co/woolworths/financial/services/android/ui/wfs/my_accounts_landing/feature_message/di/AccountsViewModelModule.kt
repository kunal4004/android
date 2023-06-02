package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.data.IMessage
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.data.IMessageImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.data.network.MessageRemoteDataSource
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.data.network.MessageRemoteDataSourceImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class AccountsViewModelModule {

    @Binds
   abstract fun provideRemoteMessage(remote: MessageRemoteDataSourceImpl): MessageRemoteDataSource

    @Binds
    abstract fun provideMessageProducer(message: IMessageImpl): IMessage

}