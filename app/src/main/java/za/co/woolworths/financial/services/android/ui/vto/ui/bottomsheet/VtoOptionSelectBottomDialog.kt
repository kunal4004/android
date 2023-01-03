package za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet

import android.content.Context
import androidx.fragment.app.Fragment
import com.awfs.coordination.databinding.SelectVtoOptionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
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
        val binding = SelectVtoOptionBinding.inflate(dialog.layoutInflater, null, false)

        binding.cancelVTO.setOnClickListener {
            dialog.dismiss()
        }
        binding.browseFiles.setOnClickListener {
            listener.browseFiles()
            dialog.dismiss()

        }
        binding.choosePhoto.setOnClickListener {
            listener.openGallery()
            dialog.dismiss()
        }

        binding.openLiveCamera.setOnClickListener {
            listener.openLiveCamera()
            dialog.dismiss()
        }

        binding.takePhoto.setOnClickListener {
            listener.openCamera()
            dialog.dismiss()
        }

        dialog.setCancelable(false)
        dialog.setContentView(binding.root)
        dialog.show()

    }
}