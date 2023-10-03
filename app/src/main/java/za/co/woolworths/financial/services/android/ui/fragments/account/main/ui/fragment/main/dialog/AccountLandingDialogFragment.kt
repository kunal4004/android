package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.databinding.AccountLandingDialogFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.base.ViewBindingDialogFragment
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.util.setDialogPadding

@AndroidEntryPoint
class AccountLandingDialogFragment :
    ViewBindingDialogFragment<AccountLandingDialogFragmentBinding>() {

    val args: AccountLandingDialogFragmentArgs by navArgs()
    val viewModel by viewModels<AccountLandingDialogViewModel>()

    override fun onStart() {
        super.onStart()
        setDialogPadding(dialog)
    }

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        AccountLandingDialogFragmentBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setArguments()
        setCaption()
        setButtonVisibility()
        setListeners()
    }

    private fun setArguments() {
        viewModel.setup(args)
    }

    private fun setCaption() {
        viewModel.dialogData?.apply {
            with(binding) {
                accountInArrearsTitleTextView.text = getString(title)
                accountInArrearsDescriptionTextView.text = getString(desc, formattedValue)
                payNowButton.text = getString(firstButtonTitle)
                chatToUsButton.text = getString(secondButtonTitle)
            }
        }
    }

    private fun setButtonVisibility() {
        viewModel.dialogData?.apply {
            with(binding) {
                if (secondButtonVisibility == VISIBLE) {
                    chatToUsButton.visibility = VISIBLE
                    payNowDivider.visibility = VISIBLE
                }
            }
        }
    }

    private fun setListeners() {
        with(binding) {

            closeIconImageButton.onClick { dismiss() }

            payNowButton.onClick { setResult(requestKeyAccountLandingDialog, viewModel.dialogData?.firstButtonTitle) }

            chatToUsButton.onClick { setResult(requestKeyAccountLandingDialog,  viewModel.dialogData?.secondButtonTitle) }

        }
    }

    private fun setResult(requestKey: String, @StringRes key: Int?) {
        setFragmentResult(requestKey, bundleOf(requestKey to key))
        dismiss()
    }

    companion object {
        val requestKeyAccountLandingDialog: String = AccountLandingDialogFragment::class.java.simpleName
    }
    
}

