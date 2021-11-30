package za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet

import android.content.Context
import androidx.fragment.app.Fragment

interface VtoBottomSheetDialog {

    fun showBottomSheetDialog(fragment: Fragment, context: Context, isFrom: Boolean)

}