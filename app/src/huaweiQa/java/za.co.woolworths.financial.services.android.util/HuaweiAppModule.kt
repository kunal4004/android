package za.co.woolworths.financial.services.android.util

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

import javax.inject.Singleton
import dagger.hilt.components.SingletonComponent
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.IMyAccountsUtils
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.MyAccountsUtils


@Module
@InstallIn(SingletonComponent::class)
object HuaweiAppModule {

    @Provides
    fun provideHuaweiQualityCrashInstance() = HuaweiQualityCrashInstance()

}