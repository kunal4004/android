package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.loan_withdrawal_minimum_amount_error_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.LoanWithdrawalPopupDialog.LoanWithdrawalPopupType.*

class LoanWithdrawalPopupDialog : WBottomSheetDialogFragment() {

    sealed class LoanWithdrawalPopupType : Parcelable {
        @Parcelize
        object TooLow : LoanWithdrawalPopupType()

        @Parcelize
        data class TooHigh(val drawnDownAmount: String) : LoanWithdrawalPopupType()

        @Parcelize
        data class LoanAmountUnavailable(val drawnDownAmount: String) : LoanWithdrawalPopupType()

        @Parcelize
        data class GenericPopup(val errorMessage: String?) : LoanWithdrawalPopupType()
    }

    companion object {
        private const val LOAN_WITHDRAWAL_ERROR_TYPE = "LOAN_WITHDRAWAL_ERROR_TYPE"
        fun newInstance(popupType: LoanWithdrawalPopupType) =
            LoanWithdrawalPopupDialog().withArgs {
                putParcelable(LOAN_WITHDRAWAL_ERROR_TYPE, popupType)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.loan_withdrawal_minimum_amount_error_fragment,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments?.getParcelable<LoanWithdrawalPopupType>(LOAN_WITHDRAWAL_ERROR_TYPE)
        val titleDescPair = when (args) {
            is TooLow -> getString(R.string.loan_withdrawal_popup_low_error) to getString(R.string.loan_request_low_desc)
            is TooHigh -> getString(R.string.loan_request_high) to getString(
                R.string.loan_request_high_desc,
                args.drawnDownAmount
            )
            is GenericPopup -> "" to args.errorMessage
            is LoanAmountUnavailable -> getString(R.string.loan_request_fund_not_available_title) to getString(
                R.string.loan_request_fund_not_available_desc
            )

            else -> null
        }

        val contentDescription = when (args) {
            is TooLow -> "titleWithdrawalAmountTooLow" to "withdrawalAmountTooLowDescription"
            is TooHigh -> "titleWithdrawalAmountTooHigh" to "withdrawalAmountTooHighDescription"
            else -> "" to "responseDescription"
        }

        titleTextView?.contentDescription = contentDescription.first
        descriptionTextView?.contentDescription = contentDescription.second

        titleDescPair?.apply {
            if (first.isEmpty()) {
                titleTextView?.visibility = GONE
                loanWithdrawalGotItButton?.text = getString(R.string.ok)
                descriptionTextView?.text = second
                return@apply
            }
            titleTextView?.text = first
            descriptionTextView?.text = second
        }

        loanWithdrawalGotItButton?.setOnClickListener { dismiss() }

    }
}