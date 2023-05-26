package za.co.woolworths.financial.services.android.ui.fragments.payflex

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.PayflexBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant

class PayFlexBottomSheetDialog : WBottomSheetDialogFragment() {
    private lateinit var binding: PayflexBottomSheetBinding
    private var mCurrentWebViewScrollY = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PayflexBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheetBehavior = bottomSheetDialog.behavior
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING && mCurrentWebViewScrollY > 0) {
                    // this is where we check if webview can scroll up or not and based on that we let BottomSheet close on scroll down
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }
        })

        binding.apply {
            val maxHeight: Int = (resources.displayMetrics.heightPixels * 0.8).toInt()
            payFlexWebView.apply {
                layoutParams.height = maxHeight
                loadUrl(AppConstant.PAYFLEX_POP_UP_URL)
            }
            gotIt.setOnClickListener {
                dismiss()
            }
        }
        addOnScrollToWebview()
    }


    private fun addOnScrollToWebview() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.payFlexWebView.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
                mCurrentWebViewScrollY = scrollY
            }
        }
    }
}


