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
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.base.ViewBindingBottomSheetFragment
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class StoreCardNotReceivedDialogFragment :
    ViewBindingBottomSheetFragment<StoreCardVtscCardNotReceivedPopupDialogBinding>() {

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
    }

    private fun setupViews() {
        with(binding) {
            actionButton.setOnClickListener { view ->
                AnimationUtilExtension.animateViewPushDown(view)
                when (actionButton.text) {
                    activity?.getString(R.string.vtsc_card_not_arrived_button_caption) -> {
                        showProgress(true)
                        viewModel.queryAPIServiceGetCardNotReceived()
                    }
                    else -> dismiss()
                }
            }
        }
    }

    private fun showProgress(isVisible: Boolean) {
        with(binding) {
            notifyCardNotReceivedProgressbar.visibility = if (isVisible) VISIBLE else GONE
            actionButton.apply {
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
                    Utils.sessionDaoSave(
                        SessionDao.KEY.CARD_NOT_RECEIVED_DIALOG_WAS_SHOWN,
                        "true"
                    )
                    successNotificationView()
                }
                is ApiResult.Error -> {
                    showProgress(false)
                    dismiss()
                } // todo:: display error dialog dismiss()
            }
        }
    }

    private fun successNotificationView() {
        with(binding) {
            headerTextView.text = getString(R.string.vtsc_card_not_arrived_notified_title)
            descriptionTextView.text = getString(R.string.vtsc_card_not_arrived_notified_desc)
            actionButton.text = getString(R.string.got_it)
        }
    }
}