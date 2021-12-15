package za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet

import android.content.Context
import androidx.fragment.app.Fragment

interface VtoErrorBottomSheetDialog {

    fun showErrorBottomSheetDialog(
        fragment: Fragment,
        context: Context,
        title: String,
        description: String,
        btnText: String
    )

}