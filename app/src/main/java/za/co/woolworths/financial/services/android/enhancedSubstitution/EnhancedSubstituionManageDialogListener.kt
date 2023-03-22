package za.co.woolworths.financial.services.android.enhancedSubstitution

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