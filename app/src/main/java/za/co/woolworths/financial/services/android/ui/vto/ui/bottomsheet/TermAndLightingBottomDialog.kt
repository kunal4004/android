package za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.vto_term_and_lighting_bottom_dialog.view.*
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import javax.inject.Inject


class TermAndLightingBottomDialog @Inject constructor(

) : VtoBottomSheetDialog {


    override fun showBottomSheetDialog(
        fragment: Fragment,
        context: Context,
        fromListingPage: Boolean
    ) {

        val lightingTipMessage = AppConfigSingleton.virtualTryOn?.lightingTipText
        val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val view = dialog.layoutInflater.inflate(R.layout.vto_term_and_lighting_bottom_dialog, null)
        view.txtLightingTips.text = context.getString(R.string.vto_try_it_on_tips)
        view.btnLightingGotIt.setOnClickListener {

            dialog.dismiss()
        }
        if (fromListingPage) {
            view.imgLighting.visibility = View.GONE
            view.txtLightingTips.text = context.getString(R.string.vto_terms_conditions)
            view.txtLightingDescription.setOnClickListener {
                openVtoTermAndConditionUrl(context)
                dialog.dismiss()
            }
        } else {
            view.txtLightingDescription.text = lightingTipMessage

        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun openVtoTermAndConditionUrl(context: Context) {
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.woolworths.co.za/corporate/cmp212408")
            context.startActivity(this)
        }
    }
}
