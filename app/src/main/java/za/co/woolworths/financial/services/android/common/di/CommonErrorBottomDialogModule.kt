package za.co.woolworths.financial.services.android.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import za.co.woolworths.financial.services.android.common.CommonErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.common.CommonErrorBottomSheetDialogImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class CommonErrorBottomDialogModule {

    @Binds
    abstract fun bindCommonErrorBottomSheet(commonErrorBottomSheetDialogImpl: CommonErrorBottomSheetDialogImpl): CommonErrorBottomSheetDialog

}

