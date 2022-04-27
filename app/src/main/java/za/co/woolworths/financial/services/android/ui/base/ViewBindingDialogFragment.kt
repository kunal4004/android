package za.co.woolworths.financial.services.android.ui.base

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.deviceWidth
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.autoCleared

abstract class ViewBindingDialogFragment<VB : ViewBinding> : AppCompatDialogFragment() {

    private var _binding: VB by autoCleared()

    val binding: VB
        get() = _binding

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = (deviceWidth() - resources.getDimension(R.dimen._24sdp)).toInt()
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
            dialog.window?.setBackgroundDrawable(
                InsetDrawable(
                    ColorDrawable(
                        ContextCompat.getColor(requireContext(),
                            R.color.transparent)), 10, 10, 10, 10)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateViewBinding(inflater, container)
        return binding.root
    }

    abstract fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
}