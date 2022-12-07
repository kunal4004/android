package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.awfs.coordination.databinding.CliErrorMessageDialogBinding
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

@Parcelize
data class ErrorMessageDialog(
    @StringRes val title: Int,
    @StringRes val desc: Int,
    @StringRes val buttonLabel: Int,
    val titleContentDesc: Int,
    val descContentDesc: Int,
    val buttonContentDesc: Int
) : Parcelable

class CLIErrorMessageButtonDialog : WBottomSheetDialogFragment() {

    companion object {
        private const val CLI_ERROR_MESSAGE_DIALOG = "CLI_ERROR_MESSAGE_DIALOG"
        fun newInstance(popupType: ErrorMessageDialog) =
            CLIErrorMessageButtonDialog().withArgs {
                putParcelable(CLI_ERROR_MESSAGE_DIALOG, popupType)
            }
    }

    private lateinit var binding: CliErrorMessageDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CliErrorMessageDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val args = arguments?.getParcelable<ErrorMessageDialog>(CLI_ERROR_MESSAGE_DIALOG)

            args?.apply {
                titleTextView?.text = getString(title)
                descriptionTextView?.text = getString(desc)
                loanWithdrawalGotItButton?.text = getString(buttonLabel)
            }

            titleTextView?.contentDescription = ""
            descriptionTextView?.contentDescription = ""
            loanWithdrawalGotItButton?.contentDescription = ""

            loanWithdrawalGotItButton?.setOnClickListener { dismiss() }
        }
    }
}