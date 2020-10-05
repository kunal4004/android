package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.root_device_info_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ErrorDialogFragment : WBottomSheetDialogFragment() {

    private var mDescription: String? = null

    companion object {
        private const val DESCRIPTION = "DESCRIPTION"
        fun newInstance(description: String) = ErrorDialogFragment().withArgs {
            putString(DESCRIPTION, description)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mDescription = getString(DESCRIPTION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.error_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvDescription?.text = mDescription

        gotITButton?.setOnClickListener { dismissAllowingStateLoss() }
    }
}