package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.DefaultErrorMessageDialogBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment.Companion.SET_DELIVERY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.KotlinUtils

class ErrorMessageDialogFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: DefaultErrorMessageDialogBinding
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
        binding = DefaultErrorMessageDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            tvErrorDescription?.text = message
            btnDismissDialog?.text = buttonLabel
            btnDismissDialog?.setOnClickListener(this@ErrorMessageDialogFragment)
        }
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
            KotlinUtils.presentEditDeliveryGeoLocationActivity(this, SET_DELIVERY_LOCATION_REQUEST_CODE)
        }
    }
}
