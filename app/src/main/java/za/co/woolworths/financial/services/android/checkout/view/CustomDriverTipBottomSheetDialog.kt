package za.co.woolworths.financial.services.android.checkout.view

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import com.awfs.coordination.R
import com.awfs.coordination.databinding.DriverTipCustomDialogBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils


/**
 * Created by Kunal Uttarwar on 25/03/22.
 */
class CustomDriverTipBottomSheetDialog : WBottomSheetDialogFragment() {

    private lateinit var binding: DriverTipCustomDialogBinding
    private var mTitle: String? = null
    private var mSubTitle: String? = null
    private lateinit var mTipValue: String

    interface ClickListner {
        fun onConfirmClick(tipValue: String)
        fun onCancelDialog(previousTipValue: String)
    }

    companion object {
        private const val TITLE = "TITLE"
        private const val SUB_TITLE = "SUB_TITLE"
        private const val TIP_VALUE = "TIP_VALUE"
        const val MIN_TIP_VALUE =
            5.00
        const val MAX_TIP_VALUE =
            1000.00
        private var clickListner: ClickListner? = null

        fun newInstance(
            title: String,
            subTitle: String,
            tipValue: String,
            listner: ClickListner,
        ) =
            CustomDriverTipBottomSheetDialog().withArgs {
                putString(TITLE, title)
                putString(SUB_TITLE, subTitle)
                putString(TIP_VALUE, tipValue)
                clickListner = listner
            }.apply {
                dialog?.window?.let{ window ->
                    val params = dialog!!.window!!.attributes
                    params.width = WindowManager.LayoutParams.MATCH_PARENT
                    params.height = WindowManager.LayoutParams.MATCH_PARENT
                    window.attributes = params
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE )
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mTitle = getString(TITLE, "")
            mSubTitle = getString(SUB_TITLE, "")
            mTipValue = getString(TIP_VALUE, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DriverTipCustomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            titleTextView?.text = mTitle
            subTitleTextView?.text = mSubTitle
            driverTipAmtEditText?.setText(mTipValue)
            driverTipAmtEditText?.filters =
                getMaxLengthFilter(
                    AppConfigSingleton.dashConfig?.driverTip?.maxAmount?.toString()?.length
                        ?: MAX_TIP_VALUE.toString().length,
                    2
                )
            if (driverTipAmtEditText?.text.isNullOrEmpty())
                Utils.fadeInFadeOutAnimation(buttonConfirm, true)
            driverTipAmtEditText.addTextChangedListener { amount ->
                when {
                    amount.isNullOrEmpty() -> {
                        Utils.fadeInFadeOutAnimation(buttonConfirm, true)
                    }
                    amount.toString()
                        .toDouble() < (AppConfigSingleton.dashConfig?.driverTip?.minAmount
                        ?: MIN_TIP_VALUE) -> {
                        // Driver tip should always be greater than or equal R5
                        Utils.fadeInFadeOutAnimation(buttonConfirm, true)
                        driverTipErrorText?.visibility = View.VISIBLE
                        driverTipErrorText?.text =
                            requireContext().getString(
                                R.string.driver_minimum_tip_amt_error,
                                AppConfigSingleton.dashConfig?.driverTip?.minAmount ?: 0.0
                            )
                    }
                    amount.toString()
                        .toDouble() > (AppConfigSingleton.dashConfig?.driverTip?.maxAmount
                        ?: MAX_TIP_VALUE) -> {
                        // Driver tip should always be less than R1000
                        Utils.fadeInFadeOutAnimation(buttonConfirm, true)
                        driverTipErrorText?.visibility = View.VISIBLE
                        driverTipErrorText?.text =
                            requireContext().getString(
                                R.string.driver_maximum_tip_amt_error,
                                AppConfigSingleton.dashConfig?.driverTip?.maxAmount ?: 0.0
                            )
                    }
                    else -> {
                        driverTipErrorText?.visibility = View.GONE
                        Utils.fadeInFadeOutAnimation(buttonConfirm, false)
                    }
                }
            }
            buttonConfirm?.setOnClickListener {
                // dismiss dialog and pass the value to original fragment
                dismiss()
                mTipValue = driverTipAmtEditText?.text.toString()
                clickListner?.onConfirmClick(mTipValue)
                val customProgressBottomSheetDialog =
                    CustomProgressBottomSheetDialog.newInstance(mTipValue)
                customProgressBottomSheetDialog.show(
                    requireFragmentManager(),
                    CustomProgressBottomSheetDialog::class.java.simpleName
                )
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        clickListner?.onCancelDialog(mTipValue)
    }

    private fun getMaxLengthFilter(lengthBeforeDecimal: Int, decimalPlace: Int): Array<InputFilter?> {
        val filterArray = arrayOfNulls<InputFilter>(2)
        filterArray[0] =  LengthFilter(lengthBeforeDecimal + decimalPlace)
        filterArray[1] =
            DecimalDigitsInputFilter(
            digitsBeforeZero = lengthBeforeDecimal,
            digitsAfterZero = decimalPlace
        )
        return filterArray
    }
}

