package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardCancelDeliveryConfirmationDialogBinding
import com.awfs.coordination.databinding.CreditCardDeliveryTooLateToEditDialogBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity.DeliveryStatus
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class CancelOrToLateDeliveryDialog : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var bindingTooLateToEdit: CreditCardDeliveryTooLateToEditDialogBinding
    private lateinit var bindingCancelDeliveryConfirmation: CreditCardCancelDeliveryConfirmationDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey("creditCardStatus")) {
                if (arguments?.getInt("creditCardStatus") == (DeliveryStatus.CANCEL_DELIVERY.value)) {
                    bindingCancelDeliveryConfirmation = CreditCardCancelDeliveryConfirmationDialogBinding.inflate(inflater, container, false)
                    return bindingCancelDeliveryConfirmation.root
                } else if (arguments?.getInt("creditCardStatus") == (DeliveryStatus.EDIT_ADDRESS.value)) {
                    bindingTooLateToEdit = CreditCardDeliveryTooLateToEditDialogBinding.inflate(inflater, container, false)
                    return bindingTooLateToEdit.root
                }
            }
        }
        return null
    }

    companion object {
        fun newInstance(deliveryStatus: DeliveryStatus) = CancelOrToLateDeliveryDialog().withArgs {
            putInt("creditCardStatus", deliveryStatus.value)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        if (this::bindingTooLateToEdit.isInitialized) {
            bindingTooLateToEdit.cancel.setOnClickListener(this)
            bindingTooLateToEdit.callCallCenter.setOnClickListener(this)
        } else if (this::bindingCancelDeliveryConfirmation.isInitialized) {
            bindingCancelDeliveryConfirmation.cancel.setOnClickListener(this)
            bindingCancelDeliveryConfirmation.callCallCenter.setOnClickListener(this)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel -> {
                dismiss()
            }
            R.id.callCallCenter -> {
                dismiss()
                activity?.apply {
                    Utils.makeCall(AppConfigSingleton.creditCardDelivery?.callCenterNumber)
                }
            }
        }
    }
}