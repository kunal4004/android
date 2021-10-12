package za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet

import android.content.Context
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.select_vto_option.view.*

import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoSelectOptionListener
import javax.inject.Inject

class VtoOptionSelectBottomDialog @Inject constructor(

) : VtoBottomSheetDialog {

    private lateinit var listener: VtoSelectOptionListener

    override fun showBottomSheetDialog(fragment: Fragment, context: Context, isFrom: Boolean) {
        try {
            listener = fragment as VtoSelectOptionListener
        } catch (e: Exception) {
        }

        val dialog = BottomSheetDialog(context)
        val view = dialog.layoutInflater.inflate(R.layout.select_vto_option, null)

        view.cancelVTO.setOnClickListener {
            dialog.dismiss()
        }
        view.browseFiles.setOnClickListener {
            listener.browseFiles()
            dialog.dismiss()

        }
        view.choosePhoto.setOnClickListener {
            listener.openGallery()
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()

    }
}