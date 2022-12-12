package za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.VtoTermAndLightingBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
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
        val binding = VtoTermAndLightingBottomDialogBinding.inflate(dialog.layoutInflater, null, false)
        binding.txtLightingTips.text = context.getString(R.string.vto_try_it_on_tips)
        binding.btnLightingGotIt.setOnClickListener {

            dialog.dismiss()
        }
        if (fromListingPage) {
            binding.imgLighting.visibility = View.GONE
            binding.txtLightingTips.text = context.getString(R.string.vto_terms_conditions)
            binding.txtLightingDescription.setOnClickListener {
                openVtoTermAndConditionUrl(context)
                dialog.dismiss()
            }
        } else {
            binding.txtLightingDescription.text = lightingTipMessage

        }
        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun openVtoTermAndConditionUrl(context: Context) {
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.woolworths.co.za/corporate/cmp212408")
            context.startActivity(this)
        }
    }
}

