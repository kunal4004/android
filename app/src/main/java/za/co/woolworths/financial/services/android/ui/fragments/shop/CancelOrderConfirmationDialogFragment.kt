package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CancelOrderConfirmationDialogBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils

class CancelOrderConfirmationDialogFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: CancelOrderConfirmationDialogBinding
    private var listener: ICancelOrderConfirmation? = null
    var isNavigatedFromMyAccounts: Boolean = false

    interface ICancelOrderConfirmation {
        fun onCancelOrderConfirmation()
    }

    companion object {
        fun newInstance(
            isNavigatedFromMyAccountFlag: Boolean
        ) = CancelOrderConfirmationDialogFragment().withArgs {
            putBoolean(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, isNavigatedFromMyAccountFlag)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CancelOrderConfirmationDialogBinding.inflate(inflater, container, false)
        return binding.root
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

        with(binding) {
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
                isNavigatedFromMyAccounts =
                    getBoolean(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, false)
            }
            initializeCancelReasonColor()
        }
    }


    fun triggerFirebaseEvent(properties: String) {
        val arguments = HashMap<String, String>()
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = properties
        Utils.triggerFireBaseEvents(if (isNavigatedFromMyAccounts) FirebaseManagerAnalyticsProperties.Acc_My_Orders_Cancel_Order else FirebaseManagerAnalyticsProperties.SHOP_MY_ORDERS_CANCEL_ORDER, arguments, requireActivity())
    }

    private fun CancelOrderConfirmationDialogBinding.setCancelButtonUI(activated: Boolean) {
        if (activated) {
            confirmCancelOrder.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.black))
            confirmCancelOrder.isEnabled = true
        } else {
            confirmCancelOrder?.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_A9A9A9))
            confirmCancelOrder.isEnabled = false
        }
    }

    private fun CancelOrderConfirmationDialogBinding.initializeCancelReasonColor() {
        cancelReasons?.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBtnNeedItems -> {
                    radioBtnNeedItems?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    radioBtnForgotSomething?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    radioBtnNeedPlace?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    radioBtnOther?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    setCancelButtonUI(true)
                }
                R.id.radioBtnForgotSomething -> {
                    radioBtnForgotSomething?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    radioBtnNeedItems?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    radioBtnNeedPlace?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    radioBtnOther?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    setCancelButtonUI(true)
                }
                R.id.radioBtnNeedPlace -> {
                    radioBtnNeedPlace?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    radioBtnForgotSomething?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    radioBtnNeedItems?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    radioBtnOther?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    setCancelButtonUI(true)
                }
                R.id.radioBtnOther -> {
                    radioBtnOther?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    radioBtnNeedPlace?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    radioBtnForgotSomething?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    radioBtnNeedItems?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.color_666666))
                    setCancelButtonUI(true)
                }
            }
        }
    }
}