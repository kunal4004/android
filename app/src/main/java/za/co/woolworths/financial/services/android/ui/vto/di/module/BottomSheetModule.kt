package za.co.woolworths.financial.services.android.ui.vto.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import za.co.woolworths.financial.services.android.ui.vto.di.qualifier.OpenSelectOption
import za.co.woolworths.financial.services.android.ui.vto.di.qualifier.OpenTermAndLighting
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.TermAndLightingBottomDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoOptionSelectBottomDialog


@Module
@InstallIn(FragmentComponent::class)
abstract class BottomSheetModule {

    @OpenTermAndLighting
    @Binds
    abstract fun bindBottomSheet(vtoBottomSheetDialogImpl: TermAndLightingBottomDialog): VtoBottomSheetDialog

    @OpenSelectOption
    @Binds
    abstract fun bindSelectOptionBottomSheet(vtoOptionSelectBottomDialog: VtoOptionSelectBottomDialog): VtoBottomSheetDialog

}