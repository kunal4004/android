package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cancel_order_confirmation_dialog.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils

class CancelOrderConfirmationDialogFragment : WBottomSheetDialogFragment() {

    private var listener: ICancelOrderConfirmation? = null
    var isNavigatedFromMyAccounts: Boolean = false

    interface ICancelOrderConfirmation {
        fun onCancelOrderConfirmation()
    }

    companion object {
        fun newInstance(isNavigatedFromMyAccountFlag: Boolean) = CancelOrderConfirmationDialogFragment().withArgs {
            putBoolean(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, isNavigatedFromMyAccountFlag)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.cancel_order_confirmation_dialog, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            listener = parentFragment as ICancelOrderConfirmation?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancel?.apply {

            triggerFirebaseEvent(FirebaseManagerAnalyticsProperties.PropertyNames.CANCEL_CANCEL)
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener { dismissAllowingStateLoss() }
        }
        confirmCancelOrder.setOnClickListener {
            triggerFirebaseEvent(FirebaseManagerAnalyticsProperties.PropertyNames.CONFIRM_CANCEL)
            listener?.onCancelOrderConfirmation()
            dismissAllowingStateLoss()
        }

        arguments?.apply {
            isNavigatedFromMyAccounts = getBoolean(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, false)
        }
    }


    fun triggerFirebaseEvent(properties: String) {
        // this  can be moved to calling places
        val arguments = HashMap<String, String>()
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = properties
        Utils.triggerFireBaseEvents(if (isNavigatedFromMyAccounts) FirebaseManagerAnalyticsProperties.Acc_My_Orders_Cancel_Order else FirebaseManagerAnalyticsProperties.SHOP_MY_ORDERS_CANCEL_ORDER, arguments, requireActivity())
    }

}