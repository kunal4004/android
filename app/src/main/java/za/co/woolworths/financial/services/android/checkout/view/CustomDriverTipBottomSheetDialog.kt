package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.driver_tip_custom_dialog.*
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 25/03/22.
 */
class CustomDriverTipBottomSheetDialog : WBottomSheetDialogFragment() {

    private var mTitle: String? = null
    private var mSubTitle: String? = null
    private lateinit var mTipValue: String

    interface ClickListner {
        fun onConfirmClick(tipValue: String)
    }

    companion object {
        private const val TITLE = "TITLE"
        private const val SUB_TITLE = "SUB_TITLE"
        private const val TIP_VALUE = "TIP_VALUE"
        private const val MIN_TIP_VALUE =
            5.00 // ToDo This will be taken from config once config is ready.
        private const val MAX_TIP_VALUE =
            1000.00 // ToDo This will be taken from config once config is ready.
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
        return inflater.inflate(R.layout.driver_tip_custom_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleTextView?.text = mTitle
        subTitleTextView?.text = mSubTitle
        driverTipAmtEditText?.setText(mTipValue)
        if (driverTipAmtEditText?.text.isNullOrEmpty())
            Utils.fadeInFadeOutAnimation(buttonConfirm, true)
        driverTipAmtEditText.addTextChangedListener { amount ->
            when {
                amount.isNullOrEmpty() -> {
                    Utils.fadeInFadeOutAnimation(buttonConfirm, true)
                }
                amount.toString().toDouble() < MIN_TIP_VALUE -> {
                    // Driver tip should always be greater than or equal R5
                    Utils.fadeInFadeOutAnimation(buttonConfirm, true)
                    driverTipErrorText?.visibility = View.VISIBLE
                    driverTipErrorText?.text =
                        requireContext().getString(R.string.driver_minimum_tip_amt_error, MIN_TIP_VALUE)
                }
                amount.toString().toDouble() > MAX_TIP_VALUE -> {
                    // Driver tip should always be less than R1000
                    Utils.fadeInFadeOutAnimation(buttonConfirm, true)
                    driverTipErrorText?.visibility = View.VISIBLE
                    driverTipErrorText?.text =
                        requireContext().getString(R.string.driver_maximum_tip_amt_error, MAX_TIP_VALUE)
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
            customProgressBottomSheetDialog.show(requireFragmentManager(),
                CustomProgressBottomSheetDialog::class.java.simpleName)
        }
    }
}
