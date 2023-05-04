package za.co.woolworths.financial.services.android.chanel.listener

import android.content.Context
import androidx.fragment.app.Fragment

interface EnhancedSubstituionManageDialogListener {
    fun showEnhancedSubstitionBottomSheetDialog(
        fragment: Fragment,
        context: Context,
        title: String,
        description: String,
        btnText: String
    )
}