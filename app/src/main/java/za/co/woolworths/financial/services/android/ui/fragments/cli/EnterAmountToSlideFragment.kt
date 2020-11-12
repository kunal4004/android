package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cli_slider_error_dialog.*
import za.co.woolworths.financial.services.android.contracts.IEditAmountSlider
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class EnterAmountToSlideFragment : WBottomSheetDialogFragment() {

    private var mTitle: String? = null
    private var mProgressValue: Int? = null
    private var mDrawnDownAmount: Int? = null
    private var mDescription: String? = null
    private var mEditAmountSlider: IEditAmountSlider? = null

    companion object {
        private const val ERROR_TITLE = "TITLE"
        private const val ERROR_DESCRIPTION = "DESCRIPTION"
        private const val PROGRESS_VALUE = "PROGRESS_VALUE"
        private const val DRAWN_DOWN_AMOUNT = "DRAWN_DOWN_AMOUNT"

        fun newInstance(amount: Int, drawnDownAmount: Int, title: String, description: String) = EnterAmountToSlideFragment().withArgs {
            putInt(PROGRESS_VALUE, amount)
            putInt(DRAWN_DOWN_AMOUNT, drawnDownAmount)
            putString(ERROR_TITLE, title)
            putString(ERROR_DESCRIPTION, description)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.apply {
            try {
                mEditAmountSlider = this as? IEditAmountSlider
            } catch (e: ClassCastException) {
                throw ClassCastException("$this must implement MyInterface ")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mProgressValue = getInt(PROGRESS_VALUE, 0)
            mTitle = getString(ERROR_TITLE, "")
            mDescription = getString(ERROR_DESCRIPTION, "")
            mDrawnDownAmount = getInt(DRAWN_DOWN_AMOUNT, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.cli_slider_error_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTitle?.text = mTitle
        tvDescription?.text = mDescription
        gotITButton?.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mEditAmountSlider?.slideAmount(mProgressValue,mDrawnDownAmount)
    }
}
