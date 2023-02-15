package za.co.woolworths.financial.services.android.ui.views.actionsheet.vouchersBottomDialog.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import za.co.woolworths.financial.services.android.ui.views.actionsheet.vouchersBottomDialog.VouchersBottomDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.vouchersBottomDialog.VouchersBottomDialogImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class VouchersBottomDialogModule {

    @Binds
    abstract fun bindVouchersBottomDialogModule(vouchersBottomDialogImpl: VouchersBottomDialogImpl): VouchersBottomDialog

}