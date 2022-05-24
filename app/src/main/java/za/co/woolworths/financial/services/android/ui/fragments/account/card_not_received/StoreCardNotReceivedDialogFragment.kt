package za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreCardVtscCardNotReceivedPopupDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.base.ViewBindingBottomSheetFragment
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

@AndroidEntryPoint
class StoreCardNotReceivedDialogFragment : ViewBindingBottomSheetFragment<StoreCardVtscCardNotReceivedPopupDialogBinding>(), View.OnClickListener {

    val viewModel: MyAccountsRemoteApiViewModel by viewModels()

    companion object {
        fun newInstance() = StoreCardNotReceivedDialogFragment()
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): StoreCardVtscCardNotReceivedPopupDialogBinding {
        return StoreCardVtscCardNotReceivedPopupDialogBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        subscribeObserver()
        queryAPIServiceGetCardNotReceived()
    }

    private fun setupViews() {
        with(binding) {
            AnimationUtilExtension.animateViewPushDown(actionButtonTextView)
            actionButtonTextView.setOnClickListener(this@StoreCardNotReceivedDialogFragment)
        }
    }

    private fun showProgress(isVisible: Boolean) {
        with(binding) {
            notifyCardNotReceivedProgressbar.visibility = if (isVisible) VISIBLE else GONE
            actionButtonTextView.apply {
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isVisible) R.color.black else R.color.white
                    )
                )
                isEnabled = !isVisible
            }
        }
    }

    private fun subscribeObserver() {
        viewModel.notifyCardNotReceived.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Success -> {
                    showProgress(false)
                    successNotificationView()
                }
                is ApiResult.Failure -> {
                    httpErrorFromServer(result.data)
                }
                is ApiResult.Error -> {
                    errorMessage()
                }
            }
        }
    }

    private fun httpErrorFromServer(response: Response?) {
        showProgress(false)
        with(binding) {
            headerTextView.text = getString(R.string.oops_err_title)
            descriptionTextView.text = response?.desc
            actionButtonTextView.text = getString(R.string.try_again)
        }
    }

    private fun errorMessage() {
        showProgress(false)
        with(binding) {
            headerTextView.text = getString(R.string.oops_err_title)
            descriptionTextView.text = getString(R.string.oops_error_message)
            actionButtonTextView.text = getString(R.string.try_again)
        }
    }

    private fun successNotificationView() {
        showProgress(false)
        with(binding) {
            headerTextView.text = getString(R.string.vtsc_card_not_arrived_notified_title)
            descriptionTextView.text = getString(R.string.vtsc_card_not_arrived_notified_desc)
            actionButtonTextView.text = getString(R.string.got_it)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.actionButtonTextView -> {
                activity ?: return
                when (binding.actionButtonTextView.text.toString().lowercase()) {
                    getString(R.string.try_again).lowercase(),
                    getString(R.string.vtsc_card_not_arrived_button_caption) -> queryAPIServiceGetCardNotReceived()
                    getString(R.string.got_it).lowercase() -> { dismiss() }
                }
            }
        }
    }

    private fun queryAPIServiceGetCardNotReceived() {
        showProgress(true)
        viewModel.queryServiceCardNotYetReceived()
    }
}