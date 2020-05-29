package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.default_error_message_dialog.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment.Companion.SET_DELIVERY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.KotlinUtils

class ErrorMessageDialogFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private var buttonLabel: String? = null
    private var message: String? = null

    companion object {
        private const val ERR_DESCRIPTION = "ERR_DESCRIPTION"
        private const val BUTTON_LABEL = "BUTTON_LABEL"
        fun newInstance(description: String, buttonLabel: String) = ErrorMessageDialogFragment().withArgs {
            putString(ERR_DESCRIPTION, description)
            putString(BUTTON_LABEL, buttonLabel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            message = getString(ERR_DESCRIPTION)
            buttonLabel = getString(BUTTON_LABEL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.default_error_message_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvErrorDescription?.text = message
        btnDismissDialog?.text = buttonLabel
        btnDismissDialog?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnDismissDialog -> openSetYourDeliveryActivity()
            else -> return
        }
        dismissAllowingStateLoss()
    }

    private fun openSetYourDeliveryActivity() {
        activity?.apply {
            KotlinUtils.presentEditDeliveryLocationActivity(this, SET_DELIVERY_LOCATION_REQUEST_CODE)
        }
    }
}
