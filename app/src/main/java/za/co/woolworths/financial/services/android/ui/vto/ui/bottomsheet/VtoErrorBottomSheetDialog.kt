package za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet

import android.content.Context

interface VtoErrorBottomSheetDialog {

    fun showErrorBottomSheetDialog(
        context: Context,
        title: String,
        description: String,
        btnText: String
    )

}