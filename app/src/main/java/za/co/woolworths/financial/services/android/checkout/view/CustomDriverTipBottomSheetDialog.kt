package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.driver_tip_custom_dialog.*
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

    companion object {
        private const val TITLE = "TITLE"
        private const val SUB_TITLE = "SUB_TITLE"
        private const val TIP_VALUE = "TIP_VALUE"
        private lateinit var clickListner: CustomProgressBottomSheetDialog.ClickListner

        fun newInstance(
            title: String,
            subTitle: String,
            tipValue: String,
            listner: CustomProgressBottomSheetDialog.ClickListner,
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
        driverTipAmtEditText.addTextChangedListener {
            Utils.fadeInFadeOutAnimation(buttonConfirm, it.isNullOrEmpty())
        }
        buttonConfirm?.setOnClickListener {
            dismiss()
            mTipValue = driverTipAmtEditText?.text.toString()
            val customProgressBottomSheetDialog = CustomProgressBottomSheetDialog.newInstance(
                getString(R.string.adding_driver_tip_text),
                getString(R.string.processing_your_request_desc), mTipValue, clickListner)
            customProgressBottomSheetDialog.show(requireFragmentManager(),
                CustomProgressBottomSheetDialog::class.java.simpleName)
        }
    }
}
