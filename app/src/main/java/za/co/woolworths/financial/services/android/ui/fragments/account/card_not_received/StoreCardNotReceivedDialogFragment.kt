package za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreCardCardNotReceivedDialogFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.base.ViewBindingBottomSheetFragment
import za.co.woolworths.financial.services.android.ui.base.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.StoreCardeNotReceivedFragment.Companion.CardNotArrivedRequestCode

@AndroidEntryPoint
class StoreCardNotReceivedDialogFragment :
    ViewBindingBottomSheetFragment<StoreCardCardNotReceivedDialogFragmentBinding>() {

    companion object {
        const val REQUEST_CODE_TRY_AGAIN : String = "REQUEST_CODE_TRY_AGAIN"
        fun newInstance() = StoreCardNotReceivedDialogFragment()
    }

    val viewModel: MyAccountsRemoteApiViewModel by viewModels()
    val args: StoreCardNotReceivedDialogFragmentArgs by navArgs()


    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): StoreCardCardNotReceivedDialogFragmentBinding {
        return StoreCardCardNotReceivedDialogFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContent(args.response)
    }


    private fun setContent(response: ServerErrorResponse?) {
        with(binding) {

            with(headerTextView) {
                text = getString(R.string.oops_err_title)
                contentDescription = getString(R.string.vtsc_oops_err_title)
            }

            with(descriptionTextView) {
                text = response?.desc ?: getString(R.string.oops_error_message)
                contentDescription = getString(R.string.vtsc_oops_error_message)
            }

            with(actionButtonTextView) {
                if (TextUtils.isEmpty(response?.desc)){
                    text =  getString(R.string.ok)
                    contentDescription =getString(R.string.vtsc_error_ok)
                    onClick { dismiss() }
                }else {
                    text =  getString(R.string.try_again)
                    contentDescription =getString(R.string.vtsc_try_again)
                    onClick {
                        setFragmentResult(CardNotArrivedRequestCode, bundleOf(CardNotArrivedRequestCode to REQUEST_CODE_TRY_AGAIN))
                        dismiss()
                    }
                }
            }
        }
    }
}